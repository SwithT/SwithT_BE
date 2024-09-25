package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.lecture.domain.GroupTime;
import com.tweety.SwithT.lecture.domain.LectureDay;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupTimeReqDto {
    private LectureDay lectureDay;
    private LocalTime startTime;
    private LocalTime endTime;

    public GroupTime toEntity(LectureGroup lectureGroup){
        return GroupTime.builder()
                .lectureGroup(lectureGroup)
                .lectureDay(this.lectureDay)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .build();
    }
}
