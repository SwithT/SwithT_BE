package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;


import java.time.LocalDate;

public class LectureGroupReqDto {
    private Long lectureId;

    private String isAvailable;

    private Integer price;

    private Integer limit;

    private String latitude;

    private String longitude;

    private LocalDate startDate;

    private LocalDate endDate;

    public LectureGroup toEntity(Lecture lecture) {
        return LectureGroup.builder()
                .lecture(lecture)
                .isAvailable(this.isAvailable)
                .price(this.price)
                .limit(this.limit)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }
}
