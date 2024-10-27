package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.lecture_apply.domain.LectureApply;
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
    private String address;
    private String detailAddress;
    private List<LectureGroupTimeResDto> times;
    private String tutorName;
    private String category;
    private int remaining;
    private List<LectureApply> lectureApplies;
}

