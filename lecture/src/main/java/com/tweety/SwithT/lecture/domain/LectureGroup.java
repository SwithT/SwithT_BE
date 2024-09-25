package com.tweety.SwithT.lecture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lecture.dto.LectureGroupListResDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LectureGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(nullable = false, columnDefinition = "char(1) default 'Y'")
    private String isAvailable;

    @Column(columnDefinition = "integer default 0")
    private Integer price;

    @Column(columnDefinition = "integer default 1")
    private Integer limitPeople;

    private String latitude;

    private String longitude;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @OneToMany(mappedBy = "lectureGroup", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<GroupTime> groupTimes = new ArrayList<>();

}
