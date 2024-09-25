package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureDetailResDto {
    private String title;
    private String contents;
    private String image;
    private Long memberId;
    private String memberName;
    private Status status;
    private Category category;
}
