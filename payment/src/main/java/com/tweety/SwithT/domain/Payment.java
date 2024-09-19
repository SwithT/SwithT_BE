package com.tweety.SwithT.domain;

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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tuteeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Long programId;

    private String impUid; // 아임포트 거래 고유번호

    private String merchantUid; // 가맹점 주문번호

    private String pgTid; // PG사 거래번호

    private String paymentMethod; // 결제수단 (pay_method)

    private boolean escrow; // 에스크로 여부

    private String applyNum; // 카드 승인번호

    private String bankCode;

    private String bankName;

    private String cardCode;

    private String cardName;

    private String cardNumber;

    private int cardQuota; // 할부 개월 수

    private String name; // 구매한 서비스명

    private BigDecimal amount; // 결제 금액

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

    private boolean cashReceiptIssued; // 현금 영수증 발급 여부
}

