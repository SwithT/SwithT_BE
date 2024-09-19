package com.tweety.SwithT.common.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class LessonPlan extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_plan_id")
    private Long id;

    @OneToOne(mappedBy = "lesson")
    private Lesson lesson;

    private String object;

    private String instructionalStrategy;

    private String textbook;

    private String target;
}
