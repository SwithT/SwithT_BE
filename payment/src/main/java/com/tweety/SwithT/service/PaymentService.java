package com.tweety.SwithT.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.tweety.SwithT.configs.IamportApiProperty;
import com.tweety.SwithT.dto.LessonResponseDto;
import com.tweety.SwithT.dto.PaymentDto;
import com.tweety.SwithT.dto.PaymentSuccessEventDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final IamportApiProperty iamportApiProperty;
    private final LessonFeign lessonFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public PaymentService(IamportApiProperty iamportApiProperty, LessonFeign lessonFeign, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.iamportApiProperty = iamportApiProperty;
        this.lessonFeign = lessonFeign;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
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
}
