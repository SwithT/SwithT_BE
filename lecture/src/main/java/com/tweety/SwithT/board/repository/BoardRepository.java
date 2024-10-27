package com.tweety.SwithT.board.repository;

import com.tweety.SwithT.board.domain.Board;

import com.tweety.SwithT.board.domain.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    // Todo : querydsl 적용 필요
    Page<Board> findAllByLectureGroupIdAndDelYn(Long lectureGroupId,Pageable pageable, String delYn);
    Page<Board> findAllByLectureGroupIdAndTypeAndDelYn(Long lectureGroupId, Pageable pageable, Type type, String delYn);
}
