package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.Category;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCreateReqDto {

    // 강의 제목
    private String title;
    // 강의 정보
    private String contents;

    private Status status;
    // 강의 분야
    private Category category;

    private LectureType lectureType;

    public Lecture toEntity(Long memberId, String memberName, String imageUrl){
        return Lecture.builder()
                .memberId(memberId)
                .memberName(memberName)
                .image(imageUrl)
                .title(this.title)
                .contents(this.contents)
                .category(this.category)
                .status(this.status)
                .lectureType(this.lectureType)
                .searchCount(0L)
                .build();
    }
}
