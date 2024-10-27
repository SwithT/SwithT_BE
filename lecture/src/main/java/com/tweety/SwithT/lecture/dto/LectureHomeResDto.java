package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.Category;
import com.tweety.SwithT.lecture.domain.LectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureHomeResDto {

    // 강의 정보
    private Long lectureId;
    private String title;
    private String contents;
    private String image;
    private Long memberId;
    private String memberName;
    private Category category;
    private LectureType lectureType;
    
    // 강의 그룹 정보
    private Long groupId;
    private Integer limitPeople;
    private Integer price;

    private String address;
    private String detailAddress;
//    private String latitude;

//    private String longitude;

    private LocalDate startDate;
    private LocalDate endDate;

    // 강의 그룹 일정
    @Builder.Default
    private List<GroupTimeResDto> lectureGroupTimes = new ArrayList<>();

    // 단체 채팅방
    private Long chatRoomId;

    private int totalDayCount; // 전체 요일 수업 개수
    private int pastDayCount;  // 현재까지 진행된 수업 개수 추가
}
