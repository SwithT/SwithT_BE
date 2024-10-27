package com.tweety.SwithT.payment.domain;

import com.tweety.SwithT.payment.dto.PaymentListDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private String impUid; // 아임포트 거래 고유번호

    private String pgTid; // PG사 거래번호

    private String paymentMethod; // 결제수단 (pay_method)

    private String applyNum; // 카드 승인번호

    private String cardCode; // 카드사 코드번호

    private String cardNumber; // 결제에 사용된 마스킹된 카드번호

    private String name; // 구매한 서비스명

    private BigDecimal amount; // 결제 금액

    private String currency; // 결제 통화 구분

    private BigDecimal cancelAmount; // 취소 금액

    @Column(nullable = false)
    private String status; // 결제 상태

    private LocalDateTime startedAt; // 결제 요청 시각

    private LocalDateTime paidAt; // 결제 완료 시각

    private LocalDateTime failedAt; // 결제 실패 시각

    private LocalDateTime cancelledAt; // 결제 취소 시각

    private String failReason; // 실패 사유

    private String cancelReason; // 취소 사유

    private String receiptUrl; // 영수증 URL

    private Long lectureGroupId; // 구매한 그룹 번호

    public PaymentListDto fromEntity(){
        return PaymentListDto.builder()
                .id(this.id)
                .name(this.name)
                .amount(this.amount)
                .paidAt(this.paidAt)
                .status(this.status)
                .receiptUrl(this.receiptUrl)
                .lectureGroupId(this.lectureGroupId)
                .build();
    }

    public void updateStatusToCancelled(){
        this.status = String.valueOf(Status.CANCELED);
    }
}

