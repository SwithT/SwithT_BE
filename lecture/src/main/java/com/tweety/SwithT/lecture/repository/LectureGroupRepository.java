package com.tweety.SwithT.lecture.repository;

import com.tweety.SwithT.lecture.domain.LectureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureGroupRepository extends JpaRepository<LectureGroup, Long> {

    Optional<LectureGroup> findByIdAndIsAvailable(Long lectureGroupId, String isAvailable);

    //과외 리스트
//    List<LectureGroup> findByLimitAndIsAvailableAndDelYn(int limit, String isAvailable, String delYn);
}
