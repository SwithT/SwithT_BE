package com.tweety.SwithT.scheduler.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.scheduler.dto.AssignmentUpdateReqDto;
import com.tweety.SwithT.scheduler.dto.ScheduleResDto;
import com.tweety.SwithT.scheduler.dto.ScheduleUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Scheduler extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate schedulerDate;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime schedulerTime;

    @Column(nullable = false)
    private String content;

    private char alertYn = 'N';

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonManagedReference // 양방향 참조에서 관리하는 쪽
    private Member member;

    private Long lectureGroupId;

    private Long lectureAssignmentId;

    public void deleteSchedule() {
        updateDelYn();
    }

    public void updateSchedule(AssignmentUpdateReqDto dto) {
        this.title = dto.getTitle();
        this.content = dto.getContents();
        this.schedulerDate = LocalDate.parse(dto.getSchedulerDate());
        this.schedulerTime = LocalTime.parse(dto.getSchedulerTime());
    }
    public Scheduler updateSchedule(ScheduleUpdateDto dto) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.schedulerDate = dto.getSchedulerDate();
        this.schedulerTime = dto.getSchedulerTime().withSecond(0).withNano(0);
        return this;
    }

    public void makingAlert() {
        this.alertYn = 'Y';
    }

    public void cancelAlert(){
        this.alertYn = 'N';
    }

    public ScheduleResDto fromEntity(){
        return ScheduleResDto.builder()
                .id(this.id)
                .title(this.title)
                .schedulerDate(this.schedulerDate)
                .schedulerTime(this.schedulerTime)
                .content(this.content)
                .alertYn(this.alertYn)
                .lectureAssignmentId(this.lectureAssignmentId)
                .lectureGroupId(this.lectureGroupId)
                .build();
    }
}
