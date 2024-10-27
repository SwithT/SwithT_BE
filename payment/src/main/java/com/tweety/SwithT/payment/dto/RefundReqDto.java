package com.tweety.SwithT.payment.dto;

import lombok.Data;

@Data
public class RefundReqDto {
    private String cancelReason;   // 환불 사유
}
