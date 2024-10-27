package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.LectureType;
import com.tweety.SwithT.lecture.domain.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TuteeMyLectureListResDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long applyId;
    private String tutorName;
    private Long tutorId;
    private Long lectureGroupId;
    private Status status;
    private int price;
    private LocalDateTime createdTime;
    private LectureType lectureType;
    private String lectureImage;
    private ReviewStatus reviewStatus;

}
