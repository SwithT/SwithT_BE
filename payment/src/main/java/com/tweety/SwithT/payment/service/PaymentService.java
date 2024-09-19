package com.tweety.SwithT.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.tweety.SwithT.common.configs.IamportApiProperty;
import com.tweety.SwithT.payment.domain.Balance;
import com.tweety.SwithT.payment.domain.Payments;
import com.tweety.SwithT.payment.domain.Status;
import com.tweety.SwithT.payment.dto.LessonResponseDto;
import com.tweety.SwithT.payment.dto.PaymentDto;
import com.tweety.SwithT.payment.dto.PaymentSuccessEventDto;
import com.tweety.SwithT.payment.repository.BalanceRepository;
import com.tweety.SwithT.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final IamportApiProperty iamportApiProperty;
    private final LessonFeign lessonFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final BalanceRepository balanceRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(IamportApiProperty iamportApiProperty, LessonFeign lessonFeign, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper, BalanceRepository balanceRepository, PaymentRepository paymentRepository) {
        this.iamportApiProperty = iamportApiProperty;
        this.lessonFeign = lessonFeign;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.balanceRepository = balanceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Boolean isPaymentComplete(PaymentDto dto) {
        IamportClient iamportClient = iamportApiProperty.getIamportClient();
        IamportResponse<Payment> paymentResponse;

        try {
            paymentResponse = iamportClient.paymentByImpUid(dto.getImpUid()); // 결제 검증
        } catch (Exception e) {
            throw new IllegalArgumentException("결제 검증 실패: " + e.getMessage());
        }

        // 결제 상태 검증
        if (!"paid".equals(paymentResponse.getResponse().getStatus())) {
            throw new IllegalArgumentException("결제가 완료되지 않았습니다.");
        }

        // 결제 금액 검증
        long paidAmount = paymentResponse.getResponse().getAmount().longValue();
        long totalPrice = dto.getPrice();

        if (paidAmount != totalPrice) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 비동기적으로 Kafka로 결제 완료 이벤트 전송
        PaymentSuccessEventDto event = new PaymentSuccessEventDto(
                paymentResponse.getResponse().getImpUid(), paidAmount);
        kafkaTemplate.send("payment-success-topic", event);

        return true;
    }

    // Feign Client를 사용하여 과외 정보 가져오기
    public LessonResponseDto getLessonInfo(Long lessonId) {
        return lessonFeign.getLessonById(lessonId);
    }

    public void handleLessonAndPayment(Long lessonId, PaymentDto paymentDto) {
        // 과외 정보를 Feign Client로 가져옴
        LessonResponseDto lessonResponse = getLessonInfo(lessonId);

        // 결제 정보 검증 후 비동기 처리
        if (isPaymentComplete(paymentDto)) {
            // 결제 성공 이벤트를 Kafka를 통해 발행
            kafkaTemplate.send("payment-complete-topic", lessonResponse);
        } else {
            throw new IllegalArgumentException("결제 실패 또는 검증 오류");
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 00시에
    public void balanceScheduler(){
        List<Balance> balanceList = balanceRepository.findByStatus(Status.STANDBY);

        LocalDate today = LocalDate.now();
        for(Balance balance: balanceList){
            LocalDate balancedDate = balance.getBalancedTime();
            balancedDate = balancedDate.plusDays(7);
            if(today.isAfter(balancedDate)){
                balance.changeStatus();
                // 여기에 tutorId 통해서 availableMoney 올려주는 이벤트 코드 작성 필요
            }
        }
    }

    @Transactional
//    일단 void로 놨음. 추후 Dto 변경 고려
    public void refund(String impUid, BigDecimal amount, String cancelReason){
        Long tuteeId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        Payments payments = paymentRepository.findByImpUid(impUid).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 주문번호"));

        if (payments.getTuteeId().equals(tuteeId)){
            IamportClient iamportClient = iamportApiProperty.getIamportClient();

            CancelData cancelData = new CancelData(impUid, true, amount);
            cancelData.setReason(cancelReason);

            try {
                iamportClient.cancelPaymentByImpUid(cancelData);
                // 여기 이후 로직 추가

            } catch (IamportResponseException e) {
                throw new RuntimeException("");
            } catch (IOException e) {
                throw new RuntimeException("");
            }

        } else{
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
    }

    public List<dto> myPaymentsList(Pageable pageable){
        Long tuteeId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        List<dto> all = new ArrayList<>();
    }
}
