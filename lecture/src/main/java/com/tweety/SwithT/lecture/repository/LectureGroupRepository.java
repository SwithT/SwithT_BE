package com.tweety.SwithT.lecture.repository;

import com.tweety.SwithT.lecture.domain.LectureGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureGroupRepository extends JpaRepository<LectureGroup, Long> {
}
