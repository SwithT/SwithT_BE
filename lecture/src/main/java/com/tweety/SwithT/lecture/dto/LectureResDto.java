package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.common.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureResDto {

    private Long id;

    private Long lectureGroupId;   // *

    private String title;

    private Status status;

}
