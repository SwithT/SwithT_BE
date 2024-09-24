package com.tweety.SwithT.lecture.repository;

import com.tweety.SwithT.lecture.domain.LectureGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureGroupRepository extends JpaRepository<LectureGroup, Long> {

}
