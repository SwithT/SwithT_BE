package com.tweety.SwithT.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundReqDto {
    private String impUid;         // 아임포트 고유 ID
    private BigDecimal amount;     // 환불 금액
    private String cancelReason;   // 환불 사유
}
