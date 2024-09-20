package com.tweety.SwithT.lecture_assignment.repository;

import com.tweety.SwithT.lecture_assignment.domain.LectureAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureAssignmentRepository extends JpaRepository<LectureAssignment, Long> {
}
