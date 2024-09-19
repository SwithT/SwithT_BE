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
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "lesson_id")
//    private Long id;

    @OneToOne
    @JoinColumn(name = "tutee_id")
    private Tutee tutee;

    private String title;

    @Column(length = 8000)
    private String contents;

    @Column(length = 5000)
    private String image;

    private String location;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonDetail> lessonDetails = new ArrayList<>();

    @OneToOne
    @JoinColumn(name="lesson_plan_id")
    private LessonPlan lessonPlan;

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonAssignment> lessonAssignments = new ArrayList<>();


}
