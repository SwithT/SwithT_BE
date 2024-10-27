package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureGroupsResDto {
    private Long lectureGroupId;
    private List<GroupTimesResDto> groupTimes;
    private String isAvailable;
    private int remaining;
    private int price;
    private String address;
    private String detailAddress;
    private LocalDate startDate;
    private LocalDate endDate;

}
