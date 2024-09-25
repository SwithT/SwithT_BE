package com.tweety.SwithT.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDto {
    private String impUid; // 결제 고유 ID
    private long price;    // 결제 금액
}
