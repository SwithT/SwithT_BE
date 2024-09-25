package com.tweety.SwithT.lecture.repository;

import com.tweety.SwithT.lecture.domain.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Page<Lecture> findAll(Specification<Lecture> specification, Pageable pageable);
}
