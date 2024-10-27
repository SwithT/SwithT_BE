package com.tweety.SwithT.lecture_apply.repository;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureApplyRepository extends JpaRepository<LectureApply, Long> {

    List<LectureApply> findByMemberIdAndLectureGroup(Long memberId, LectureGroup lectureGroup);

    Page<LectureApply> findByLectureGroup(LectureGroup lectureGroup, Pageable pageable);
    List<LectureApply> findByLectureGroup(LectureGroup lectureGroup);
    List<LectureApply> findByLectureGroupAndStatus(LectureGroup lectureGroup, Status status);

    Page<LectureApply> findAll(Specification<LectureApply> specification, Pageable pageable);


    List<LectureApply> findByLectureGroupAndStatusAndDelYn(LectureGroup lectureGroup, Status status, String delYn);

    Optional<LectureApply> findByIdAndDelYn(Long id, String delYn);

    Optional<LectureApply> findByLectureGroupId(Long lectureGroupId);
    @Query("SELECT la FROM LectureApply la WHERE la.lectureGroup.id = :lectureGroupId AND la.status = 'ADMIT'")
    List<LectureApply> findMemberIdsByLectureGroupIdAndStatusAdmit(@Param("lectureGroupId") Long lectureGroupId);

    @Query("SELECT COUNT(l) FROM LectureApply l WHERE l.lectureGroup.id = :lectureGroupId")
    long countByLectureGroupId(@Param("lectureGroupId") Long lectureGroupId);

    List<LectureApply> findByStatusAndDelYn(Status status, String n);
}
