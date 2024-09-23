package com.tweety.SwithT.lecture_apply.dto;


import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleLectureApplySavedDto {
    //과외 신청 양식
    private Long lectureGroupId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String location;

    public LectureApply toEntity(LectureGroup lectureGroup, Long memberId){
        return LectureApply.builder()
                .lectureGroup(lectureGroup)
                .memberId(memberId)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .status(Status.STANDBY)
                .location(this.location)
                .build();
    }

}
