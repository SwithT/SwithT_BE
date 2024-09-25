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
//    private Long memberId;
    private String memberName;
    // 강의 제목
    private String title;
    // 강의 정보
    private String contents;
    // 강의 썸네일
    private String image;

    private Status status;
    // 강의 분야
    private Category category;

    private LectureType lectureType;



    public Lecture toEntity(Long memberId){
        return Lecture.builder()
                .memberId(memberId)
                .memberName(this.memberName)    // 수정 필요함
                .title(this.title)
                .contents(this.contents)
                .image(this.image)
                .category(this.category)
                .status(this.status)
                .lectureType(this.lectureType)
                .build();
    }
}
