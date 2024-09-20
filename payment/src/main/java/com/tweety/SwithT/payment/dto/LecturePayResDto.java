package com.tweety.SwithT.payment.dto;

import lombok.Data;

@Data
public class LecturePayResDto {
    private Long id;
    private String title;
    private String impUid; // 결제 고유 ID
    private long price;    // 결제 금액
}