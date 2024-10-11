package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureGroupResDto {
    private String title;
    private String image;
    private String longitude;
    private String latitude;
    private List<LectureGroupTimeResDto> times;
    private String tutorName;
    private String category;
}
