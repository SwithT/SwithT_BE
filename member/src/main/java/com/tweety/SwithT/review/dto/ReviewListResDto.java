package com.tweety.SwithT.review.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListResDto {

    private Long id;
    private BigDecimal star;
    private String title;
    private String contents;
    @JsonFormat(pattern = "yyyy-MM-dd") // 날짜를 "yyyy-MM-dd" 형식으로 변환
    private LocalDateTime createdTime;
    private Long writerId;
    @Column(nullable = true)
    private String profileImage;

    @Column(nullable = true) // 동명이인 고려
    private String name;

}
