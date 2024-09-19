package com.tweety.SwithT.lecture.repository;

import com.tweety.SwithT.lecture.domain.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

}
