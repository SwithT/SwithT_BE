package com.tweety.SwithT.lesson_apply.repository;

import com.tweety.SwithT.lesson_apply.domain.LessonApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonApplyRepository extends JpaRepository<LessonApply, Long> {
}
