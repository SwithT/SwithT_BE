package com.tweety.SwithT.lecture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lecture_assignment.domain.LectureAssignment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Lecture extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tutorId;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String contents;
    private String image;

    // 강의 시작일
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    // 강의 종료일
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // 강의 위치
    @Column(nullable = false)
    private String longitude;
    @Column(nullable = false)
    private String latitude;

    // 강의 최대 인원수
    @Column(nullable = false)
    private Integer limit;

    // 강의 상태
    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Category category;


    @Builder.Default
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.PERSIST)
    private List<LectureDetail> lectureDetails = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.PERSIST)
    private List<LectureAssignment> lectureAssignments = new ArrayList<>();

}
