package com.tweety.SwithT.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyLectureSearchDto {
    private String category;
    private String status;
    private String lectureType;
}
