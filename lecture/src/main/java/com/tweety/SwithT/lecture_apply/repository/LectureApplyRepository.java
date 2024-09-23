package com.tweety.SwithT.lecture_apply.repository;

import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureApplyRepository extends JpaRepository<LectureApply, Long> {
}
