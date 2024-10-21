package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureGroupReqDto {

    private Integer price;

    private Integer limitPeople;
    private String address;

//    private String latitude;

//    private String longitude;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<GroupTimeReqDto> groupTimeReqDtos;

    public LectureGroup toEntity(Lecture lecture) {
        return LectureGroup.builder()
                .lecture(lecture)
                .price(this.price)
                .isAvailable("Y")
                .limitPeople(this.limitPeople)
                .address(this.address)
                .remaining(this.limitPeople)
//                .latitude(this.latitude)
//                .longitude(this.longitude)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }



}
