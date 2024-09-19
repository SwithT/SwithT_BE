package com.tweety.SwithT.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class LessonAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_plan_id")
    private Long id;

    @OneToOne(mappedBy = "lesson")
    private Lesson lesson;

    @Column(nullable = false)
    private String title;

    private String contents;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column
    private LocalTime endTime;

}
