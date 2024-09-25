package com.tweety.SwithT.lecture.domain;

import com.tweety.SwithT.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GroupTime extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_group_id")
    private LectureGroup lectureGroup;

    @Enumerated(EnumType.STRING)
    private LectureDay lectureDay;

    private LocalTime startTime;

    private LocalTime endTime;
}
