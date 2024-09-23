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
import com.tweety.SwithT.payment.dto.*;
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
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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

        Boolean status = isPaymentComplete(paymentDto, lecturePayResDto);

        if (status) {
            CommonResDto returnResDto = new CommonResDto(
                    HttpStatus.OK, "결제가 성공적으로 완료되었습니다.", status);
            try {
                lectureFeign.paidStatus(returnResDto);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Feign 통신 중 오류 발생: " + e.getMessage());
            }
            return returnResDto;
        } else {
            CommonResDto returnResDto = new CommonResDto(
                    HttpStatus.BAD_REQUEST, "결제에 실패했습니다.", status);
            lectureFeign.paidStatus(returnResDto);
            return returnResDto;
        }
    }

    private Boolean isPaymentComplete(PaymentDto dto, LecturePayResDto lecturePayResDto) {
        IamportClient iamportClient = iamportApiProperty.getIamportClient();
        IamportResponse<Payment> paymentResponse;

        try {
            paymentResponse = iamportClient.paymentByImpUid(dto.getImpUid()); // 결제 검증
        } catch (IamportResponseException e) {
            throw new IllegalArgumentException("결제 검증 실패: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("결제 검증 중 IO 오류 발생", e);
        }

        Payment payment = paymentResponse.getResponse();

        // 결제 상태 검증
        if (!"paid".equals(payment.getStatus())) {
            throw new IllegalArgumentException("결제가 완료되지 않았습니다.");
        }

        // 결제 금액 검증
        long paidAmount = payment.getAmount().longValue();
        long lecturePrice = dto.getPrice();

        if (paidAmount != lecturePrice) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다. (지불 금액: " + paidAmount + ", 예상 금액: " + lecturePrice + ")");
        }

        // 결제 정보 저장
        Payments payments = Payments.builder()
                .memberId(lecturePayResDto.getMemberId())
                .impUid(payment.getImpUid())
                .pgTid(payment.getPgTid())
                .paymentMethod(payment.getPayMethod())
                .applyNum(payment.getApplyNum())
                .cardCode(payment.getCardCode())
                .cardNumber(payment.getCardNumber())
                .name(payment.getName())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .cancelAmount(payment.getCancelAmount() == null ? BigDecimal.ZERO : payment.getCancelAmount())
                .status(payment.getStatus())
                .startedAt(convertUnixToLocalDateTime(payment.getStartedAt()))
                .paidAt(convertDateToLocalDateTime(payment.getPaidAt()))
                .failedAt(payment.getFailedAt() != null ? convertDateToLocalDateTime(payment.getFailedAt()) : null)
                .cancelledAt(payment.getCancelledAt() != null ? convertDateToLocalDateTime(payment.getCancelledAt()) : null)
                .failReason(payment.getFailReason())
                .cancelReason(payment.getCancelReason())
                .receiptUrl(payment.getReceiptUrl())
                .build();

        paymentRepository.save(payments);

        return true;
    }

    // Feign Client를 사용하여 과외 정보 가져오기
    private CommonResDto getLecturePayInfo(Long lecturePayId) {
        return lectureFeign.getLectureById(lecturePayId);
    }

    // Unix 타임 (long) -> LocalDateTime 변환 메서드
    private LocalDateTime convertUnixToLocalDateTime(long unixTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault());
    }

    // Date -> LocalDateTime 변환 메서드
    private LocalDateTime convertDateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 00시에
    public void balanceScheduler() {
        List<Balance> balanceList = balanceRepository.findByStatus(Status.STANDBY);
        LocalDate today = LocalDate.now();

        for (Balance balance : balanceList) {
            LocalDate balancedDate = balance.getBalancedTime().plusDays(7);

            if (today.isAfter(balancedDate)) {
                balance.changeStatus();
                balanceRepository.save(balance);

                // memberId 통해서 availableMoney 올려주는 이벤트 코드
                BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(balance.getMemberId(), balance.getCost());

                // Kafka 전송 비동기 처리 (재시도 포함: 3번까지 전송)
                sendMessageWithRetry("balance-update-topic", balanceUpdateDto, 3);
            }
        }
    }

    private void sendMessageWithRetry(String topic, BalanceUpdateDto balanceUpdateDto, int retryCount) {
        kafkaTemplate.send(topic, balanceUpdateDto)
                .thenAccept(result -> {
                    // 성공 시 특별한 처리 필요 없음
                })
                .exceptionally(ex -> {
                    if (retryCount > 0) {
                        // 전송 실패 시 재시도
                        System.err.println("Kafka 전송 실패. 남은 재시도 횟수: " + retryCount + ", 이유: " + ex.getMessage());
                        sendMessageWithRetry(topic, balanceUpdateDto, retryCount - 1);
                    } else {
                        // 재시도 횟수가 모두 소진된 경우 예외 처리
                        System.err.println("Kafka 전송 실패. 재시도 횟수 초과: " + ex.getMessage());
                    }
                    return null;  // 처리 후 null 반환
                });
    }

    @Transactional
    public void refund(String impUid, BigDecimal amount, String cancelReason) {
        Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        // 결제 정보 조회
        Payments payments = paymentRepository.findByImpUid(impUid).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 주문번호"));

        // 결제된 멤버가 현재 로그인한 멤버와 동일한지 확인
        if (!payments.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        // 환불 가능 기간(7일) 체크
        LocalDateTime paymentDate = payments.getPaidAt();
        LocalDateTime now = LocalDateTime.now();

        if (paymentDate == null || paymentDate.plusDays(7).isBefore(now)) {
            throw new IllegalArgumentException("결제 후 7일이 경과하여 환불이 불가능합니다.");
        }

        IamportClient iamportClient = iamportApiProperty.getIamportClient();
        CancelData cancelData = new CancelData(impUid, true, amount);
        cancelData.setReason(cancelReason);

        try {
            // 결제 취소 요청
            IamportResponse<Payment> response = iamportClient.cancelPaymentByImpUid(cancelData);
            Payment cancelledPayment = response.getResponse();

            if (cancelledPayment != null && "cancelled".equals(cancelledPayment.getStatus())) {
                payments.updateStatusToCancelled();
                paymentRepository.save(payments);

                // Feign Client를 통해 환불 상태 업데이트 요청
                RefundReqDto refundRequest = new RefundReqDto();
                refundRequest.setImpUid(impUid);
                refundRequest.setAmount(amount);
                refundRequest.setCancelReason(cancelReason);
                lectureFeign.refundStatus(refundRequest);
            } else {
                throw new RuntimeException("결제 취소 중 오류 발생: 결제 상태를 확인할 수 없습니다.");
            }
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("결제 취소 중 오류 발생: " + e.getMessage());
        }
    }

    public Page<PaymentListDto> myPaymentsList(int page, int size) {
        Long tuteeId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        int validPage = Math.max(page, 0);
        int validSize = Math.min(size, 100);

        Pageable pageable = PageRequest.of(validPage, validSize);

        Page<Payments> paymentList = paymentRepository.findByTuteeId(pageable, tuteeId);
        Page<PaymentListDto> dtos = paymentList.map(Payments::fromEntity);

        return dtos;
    }
}
