package com.tweety.SwithT.board.service;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import com.tweety.SwithT.board.dto.create.BoardCreateRequest;
import com.tweety.SwithT.board.dto.create.BoardCreateResponse;
import com.tweety.SwithT.board.dto.read.BoardDetailResponse;
import com.tweety.SwithT.board.dto.read.BoardListResponse;
import com.tweety.SwithT.board.dto.update.BoardUpdateRequest;
import com.tweety.SwithT.board.dto.update.BoardUpdateResponse;
import com.tweety.SwithT.board.repository.BoardRepository;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final LectureGroupRepository lectureGroupRepository;
    public BoardService(BoardRepository boardRepository, LectureGroupRepository lectureGroupRepository) {
        this.boardRepository = boardRepository;
        this.lectureGroupRepository = lectureGroupRepository;
    }

    //create
    public BoardCreateResponse createBoard(Long lectureGroupId, BoardCreateRequest dto){
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
//        member id랑 member name 나중에 위에 사용해서 받는 건가..?
        LectureGroup lectureGroup = lectureGroupRepository.findById(lectureGroupId).orElseThrow(()-> new EntityNotFoundException("해당 강의 그룹이 없습니다."));
        Board savedBoard = boardRepository.save(BoardCreateRequest.toEntity(lectureGroup,dto));
        return BoardCreateResponse.fromEntity(savedBoard);
    }

    public Page<BoardListResponse> boardList(Long lectureGroupId,Pageable pageable){
        Page<Board> boardList = boardRepository.findAllByLectureGroupId(lectureGroupId,pageable);
        return boardList.map(BoardListResponse::fromEntity);
    }

    public BoardDetailResponse boardDetail(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("해당 게시글이 없습니다."));
        return BoardDetailResponse.fromEntity(board);
    }
    @Transactional
    public BoardUpdateResponse updateBoard(Long boardId, BoardUpdateRequest dto){
        // Todo 멤버 체크
//        String email = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();
//        member id랑 member name 나중에 위에 사용해서 받는 건가..?
        Board board = boardRepository.findById(boardId).orElseThrow(()-> new EntityNotFoundException("해당 게시글이 없습니다."));
        board.updateBoard(dto);
//        Board savedBoard = boardRepository.save(BoardUpdateRequest.toEntity(dto));
        return BoardUpdateResponse.fromEntity(board);
    }

    public String boardDelete(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("해당 게시글이 없습니다."));
        String title = board.getTitle();
        board.set
        boardRepository.deleteById(boardId);
        return "제목 : "+title+" 의 게시글이 삭제되었습니다.";
    }





}
