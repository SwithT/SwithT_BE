package com.tweety.SwithT.payment.controller;

import com.tweety.SwithT.common.dto.CommonErrorDto;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.payment.dto.PaymentListDto;
import com.tweety.SwithT.payment.dto.RefundReqDto;
import com.tweety.SwithT.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/ispaid/{id}")
    public ResponseEntity<?> isPaidAvailable(@PathVariable("id") Long lecturePayId) {
        try {
            // 결제 처리 로직
            CommonResDto commonResDto = paymentService.handleLessonAndPayment(lecturePayId);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // 잘못된 요청 (예: 금액 불일치, 결제 상태 문제)
            CommonErrorDto commonErrorDto = new CommonErrorDto(
                    HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        } catch (ResponseStatusException e) {
            // Feign 통신 또는 기타 특정 오류에 대한 처리
            CommonErrorDto commonErrorDto = new CommonErrorDto(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getReason());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            // 그 외의 모든 런타임 예외 처리
            CommonErrorDto commonErrorDto = new CommonErrorDto(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<?> processRefund(
            @RequestBody RefundReqDto refundReqDto // 환불 요청에 필요한 데이터
    ) {
        try {
            // 환불 처리
            paymentService.refund(refundReqDto.getImpUid(), refundReqDto.getAmount(), refundReqDto.getCancelReason());
            CommonResDto commonResDto = new CommonResDto(
                    HttpStatus.OK, "환불 처리 완료", refundReqDto);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(
                    HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 나의 결제 내역 조회 API 추가
    @GetMapping("/my-payments")
    public ResponseEntity<?> myPaymentsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PaymentListDto> payments = paymentService.myPaymentsList(page, size);
            return new ResponseEntity<>(payments, HttpStatus.OK);
        } catch (RuntimeException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}