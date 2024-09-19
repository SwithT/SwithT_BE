package com.tweety.SwithT.common.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Lesson extends BaseTimeEntity {
    @Column(nullable = false)
    private Long tutorId;

    @Column(nullable = false)
    private String tutorName;

    private String title;

    @Column(length = 8000)
    private String contents;

    @Column(length = 5000)
    private String image;

    private String location;

    private int price;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.STANDBY;

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.PERSIST)
    private List<LessonDetail> lessonDetails = new ArrayList<>();

    @OneToOne
    @JoinColumn(name="lesson_plan_id")
    private LessonPlan lessonPlan;

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.PERSIST)
    private List<LessonAssignment> lessonAssignments = new ArrayList<>();


}
