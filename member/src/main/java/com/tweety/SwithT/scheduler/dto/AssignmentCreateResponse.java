package com.tweety.SwithT.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.scheduler.domain.Scheduler;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentCreateResponse {
    private Long id;

    private String title;

    private LocalDate schedulerDate;

    private LocalTime schedulerTime;

    private String content;

    private char alertYn ;

    private Long memberId;

    private Long lectureGroupId;

    private Long lectureAssignmentId;

}