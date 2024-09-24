package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureGroupListResDto {
    private String title;
    private Long lectureGroupId;
}
