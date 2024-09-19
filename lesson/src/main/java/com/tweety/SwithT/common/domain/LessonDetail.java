package com.tweety.SwithT.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class LessonDetail extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    private LessonDetail lessonDetail;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column
    private LocalTime startTime;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column
    private LocalTime endTime;

    @Column(nullable = false, columnDefinition = "char(1) default 'N'")
    private String isAvailable = "Y";

}
