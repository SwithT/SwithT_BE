package com.tweety.SwithT.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.tweety.SwithT.common.configs.IamportApiProperty;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.payment.domain.Balance;
import com.tweety.SwithT.payment.domain.Payments;
import com.tweety.SwithT.payment.domain.Status;
import com.tweety.SwithT.payment.dto.LecturePayResDto;
import com.tweety.SwithT.payment.dto.PaymentDto;
import com.tweety.SwithT.payment.dto.PaymentListDto;
import com.tweety.SwithT.payment.repository.BalanceRepository;
import com.tweety.SwithT.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final IamportApiProperty iamportApiProperty;
    private final LectureFeign lectureFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final BalanceRepository balanceRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(IamportApiProperty iamportApiProperty, LectureFeign lectureFeign, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper, BalanceRepository balanceRepository, PaymentRepository paymentRepository) {
        this.iamportApiProperty = iamportApiProperty;
        this.lectureFeign = lectureFeign;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.balanceRepository = balanceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public CommonResDto handleLessonAndPayment(Long lecturePayId) {
        // 과외 정보를 Feign Client로 가져옴
        CommonResDto commonResDto = getLecturePayInfo(lecturePayId);
        LecturePayResDto lecturePayResDto = objectMapper.convertValue(
                commonResDto.getResult(), LecturePayResDto.class);

        PaymentDto paymentDto = PaymentDto.builder()
                .impUid(lecturePayResDto.getImpUid())
                .price(lecturePayResDto.getPrice())
                .build();

        Boolean status = isPaymentComplete(paymentDto);

        if (status) {
            CommonResDto returnResDto = new CommonResDto(
                    HttpStatus.OK, "결제가 성공적으로 완료되었습니다.", status);
            lectureFeign.paidStatus(returnResDto);
            return returnResDto;
        } else {
            CommonResDto returnResDto = new CommonResDto(
                    HttpStatus.OK, "결제에 실패했습니다.", status);
            lectureFeign.paidStatus(returnResDto);
            return returnResDto;
        }
    }

    private Boolean isPaymentComplete(PaymentDto dto) {
        IamportClient iamportClient = iamportApiProperty.getIamportClient();
        IamportResponse<Payment> paymentResponse;

        try {
            paymentResponse = iamportClient.paymentByImpUid(dto.getImpUid()); // 결제 검증
        } catch (IamportResponseException e) {
            throw new IllegalArgumentException("결제 검증 실패: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 결제 상태 검증
        if (!"paid".equals(paymentResponse.getResponse().getStatus())) {
            throw new IllegalArgumentException("결제가 완료되지 않았습니다.");
        }

        // 결제 금액 검증
        long paidAmount = paymentResponse.getResponse().getAmount().longValue();
        long lecturePrice = dto.getPrice();

        if (paidAmount != lecturePrice) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        return true;
    }

    // Feign Client를 사용하여 과외 정보 가져오기
    private CommonResDto getLecturePayInfo(Long lecturePayId) {
        return lectureFeign.getLectureById(lecturePayId);
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

    public Page<PaymentListDto> myPaymentsList(int page, int size){
        Long tuteeId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        Pageable pageable = PageRequest.of(page, size);

        Page<Payments> paymentList = paymentRepository.findByTuteeId(pageable, tuteeId);
        Page<PaymentListDto> dtos = paymentList.map(Payments::fromEntity);

        return dtos;
    }
}
