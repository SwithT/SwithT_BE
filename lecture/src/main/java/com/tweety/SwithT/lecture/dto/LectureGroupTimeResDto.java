package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureGroupTimeResDto {
    private String startTime;
    private String endTime;
    private String lectureDay;
}
