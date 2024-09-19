package com.tweety.SwithT.dto;

import lombok.Data;

@Data
public class PaymentDto {
    private String impUid; // 결제 고유 ID
    private long price;    // 결제 금액
}
