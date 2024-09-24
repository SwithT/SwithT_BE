package com.tweety.SwithT.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateDto {
    private Long memberId;
    private Integer balance;
}
