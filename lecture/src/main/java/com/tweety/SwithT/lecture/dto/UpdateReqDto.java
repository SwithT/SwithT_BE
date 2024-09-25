package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReqDto {

    private LectureUpdateReqDto lectureUpdateReqDto;
    private List<LectureGroupReqDto> lectureGroupReqDtos;

}
