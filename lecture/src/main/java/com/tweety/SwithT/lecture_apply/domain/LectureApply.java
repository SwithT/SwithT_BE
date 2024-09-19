package com.tweety.SwithT.lecture_apply.domain;

import com.tweety.SwithT.lecture.domain.Lecture;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LectureApply {

    private Long id;
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;
    private Long tuteeId;
}
