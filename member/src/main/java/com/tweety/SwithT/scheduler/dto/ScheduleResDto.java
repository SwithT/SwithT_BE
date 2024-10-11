package com.tweety.SwithT.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleResDto {
    private Long id;
    private String title;
    private LocalDate schedulerDate;
    private LocalTime schedulerTime;
    private String content;
    private char alertYn = 'N';
    private Long lectureGroupId;
    private Long lectureAssignmentId;

}
