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
public class LessonDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_detail_id")
    private Long id;

    @OneToOne(mappedBy = "lesson")
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
    private String status = "N";

}
