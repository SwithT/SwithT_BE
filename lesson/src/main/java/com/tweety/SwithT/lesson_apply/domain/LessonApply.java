package com.tweety.SwithT.lesson_apply.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonApply extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //튜티 id, name은 member에서 가져옴
    @Column(nullable = false)
    private Long tuteeId;

    @Column(nullable = false)
    private String tuteeName;

    //여기서 join 걸어주기
    @Column(nullable = false)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.STANDBY;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String location;

    @OneToMany(mappedBy = "lessonApply", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<LessonApplyDetail> lessonApplyDetailList = new ArrayList<>();


}
