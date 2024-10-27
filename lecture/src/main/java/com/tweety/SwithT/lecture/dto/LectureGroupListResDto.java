package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureGroupListResDto {
    private String title;
    private Long lectureGroupId;
    private String memberName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int price;
    private int limitPeople;
}
