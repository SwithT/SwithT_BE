package com.tweety.SwithT.board.repository;

import com.tweety.SwithT.board.domain.Board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    // Todo : querydsl 적용 필요
    Page<Board> findAllByLectureGroupId(Long lectureGroupId,Pageable pageable);
}
