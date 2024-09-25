package com.tweety.SwithT.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentListDto {
    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private String receiptUrl;
}
