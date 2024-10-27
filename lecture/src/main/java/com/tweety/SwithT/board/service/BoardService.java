package com.tweety.SwithT.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import com.tweety.SwithT.board.dto.create.BoardCreateReqDto;
import com.tweety.SwithT.board.dto.create.BoardCreateResDto;
import com.tweety.SwithT.board.dto.delete.BoardDeleteResDto;
import com.tweety.SwithT.board.dto.read.BoardDetailResDto;
import com.tweety.SwithT.board.dto.read.BoardListResDto;
import com.tweety.SwithT.board.dto.update.BoardUpdateReqDto;
import com.tweety.SwithT.board.dto.update.BoardUpdateResDto;
import com.tweety.SwithT.board.repository.BoardRepository;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.dto.MemberNameResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class BoardService {
    @Value("${jwt.secretKey}")
    private String secretKey;

    private final BoardRepository boardRepository;
    private final LectureGroupRepository lectureGroupRepository;
    private final MemberFeign memberFeign;
    public BoardService(BoardRepository boardRepository , LectureGroupRepository lectureGroupRepository, MemberFeign memberFeign) {

        this.boardRepository = boardRepository;
        this.lectureGroupRepository = lectureGroupRepository;
        this.memberFeign = memberFeign;
    }

    //create
    public BoardCreateResDto createBoard(Long lectureGroupId, BoardCreateReqDto dto){
        // securityContextHolder에서 member id 가져옴
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // feign으로 member 이름 가져옴
        CommonResDto commonResDto = memberFeign.getMemberNameById(memberId);
        ObjectMapper objectMapper = new ObjectMapper();
        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
        String memberName = memberNameResDto.getName();

        LectureGroup lectureGroup = lectureGroupRepository.findById(lectureGroupId).orElseThrow(()-> new EntityNotFoundException("해당 강의 그룹이 없습니다."));
        Board savedBoard = boardRepository.save(BoardCreateReqDto.toEntity(memberId, memberName, lectureGroup,dto));
        return BoardCreateResDto.fromEntity(savedBoard);
    }

    public Page<BoardListResDto> boardList(Long lectureGroupId, Pageable pageable, String type){
        // Sort by createdTime in descending order
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdTime")
        );
        // securityContextHolder에서 member id 가져옴
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        // param으로 type이 all인지 notice인지 받기..!
        // notice
        Page<Board> boardList = null;
        if(Objects.equals(type, "notice")) boardList = boardRepository.findAllByLectureGroupIdAndTypeAndDelYn(lectureGroupId,sortedPageable, Type.NOTICE, "N");
        else if(type==null) boardList = boardRepository.findAllByLectureGroupIdAndDelYn(lectureGroupId,sortedPageable, "N");
        else System.out.println("없음");
        return boardList.map(board -> BoardListResDto.fromEntity(board, memberId));
    }

    public BoardDetailResDto boardDetail(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("해당 게시글이 없습니다."));
        // Todo : comments도 한번에 보여주기
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return BoardDetailResDto.fromEntity(board,loginMemberId);
    }
    @Transactional
    public BoardUpdateResDto updateBoard(Long boardId, BoardUpdateReqDto dto)  {
        Board board = boardRepository.findById(boardId).orElseThrow(()-> new EntityNotFoundException("해당 게시글이 없습니다."));

        // 작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!board.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 게시글을 작성한 회원만 수정이 가능합니다.");
        board.updateBoard(dto);
        return BoardUpdateResDto.fromEntity(board);
    }

    public BoardDeleteResDto boardDelete(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("해당 게시글이 없습니다."));

        // 작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!board.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 게시글을 작성한 회원만 삭제가 가능합니다.");
        board.updateDelYn();
        Board saveBoard = boardRepository.save(board);
        return BoardDeleteResDto.fromEntity(saveBoard);
    }





}
