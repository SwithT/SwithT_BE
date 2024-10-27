package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.Category;
import com.tweety.SwithT.lecture.domain.LectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureDetailResDto {
    private Long id;
    private String title;
    private String contents;
    private String image;
    private Long memberId;
    private String memberName;
    private BigDecimal avgScore;
    private Status status;
    private Category category;
    private LectureType lectureType;
    private Long searchCount;
}
