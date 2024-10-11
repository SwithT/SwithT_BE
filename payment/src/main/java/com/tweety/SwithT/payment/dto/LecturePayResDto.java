package com.tweety.SwithT.payment.dto;

import lombok.Data;

@Data
public class LecturePayResDto {
    private Long id;         // 과외(강의) applyID
    private Long memberId;    // 결제한 회원 ID (튜터 또는 튜티)
    private String title;     // 강의 제목
    private String impUid;    // 결제 고유 ID
    private long price;       // 결제 금액
}