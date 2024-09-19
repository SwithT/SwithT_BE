package com.tweety.SwithT.lesson_apply.domain;

import com.tweety.SwithT.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonApplyDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_apply_id")
    private LessonApply lessonApply;

    //과외 상세의 id. 신청한 과외 시간대가 나와있음
    @Column(nullable = false)
    @JoinColumn(name = "lesson_detail_id")
    private LessonDetail lessonDetail;




}
