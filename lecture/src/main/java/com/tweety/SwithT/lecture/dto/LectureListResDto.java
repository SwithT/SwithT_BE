package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.lecture.domain.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureListResDto {
    private Long id;
    private String title;
    private String memberName;
    private Long memberId;
    private String image;



}
