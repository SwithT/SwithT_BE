package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReqDto {
    private LectureCreateReqDto lectureReqDto;
    private List<LectureGroupReqDto> lectureGroupReqDtos;
}
