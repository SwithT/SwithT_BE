package com.tweety.SwithT.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.dto.create.BoardCreateRequest;
import com.tweety.SwithT.board.dto.create.BoardCreateResponse;
import com.tweety.SwithT.board.dto.delete.BoardDeleteResponse;
import com.tweety.SwithT.board.dto.read.BoardDetailResponse;
import com.tweety.SwithT.board.dto.read.BoardListResponse;
import com.tweety.SwithT.board.dto.update.BoardUpdateRequest;
import com.tweety.SwithT.board.dto.update.BoardUpdateResponse;
import com.tweety.SwithT.board.repository.BoardRepository;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.dto.MemberNameResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public BoardCreateResponse createBoard( Long lectureGroupId, BoardCreateRequest dto){
        // securityContextHolder에서 member id 가져옴
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // feign으로 member 이름 가져옴
        CommonResDto commonResDto = memberFeign.getMemberNameById(memberId);
        ObjectMapper objectMapper = new ObjectMapper();
        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
        String memberName = memberNameResDto.getName();

        LectureGroup lectureGroup = lectureGroupRepository.findById(lectureGroupId).orElseThrow(()-> new EntityNotFoundException("해당 강의 그룹이 없습니다."));
        Board savedBoard = boardRepository.save(BoardCreateRequest.toEntity(memberId, memberName, lectureGroup,dto));
        return BoardCreateResponse.fromEntity(savedBoard);
    }

    public Page<BoardListResponse> boardList(Long lectureGroupId, Pageable pageable){
        Page<Board> boardList = boardRepository.findAllByLectureGroupId(lectureGroupId,pageable);
        return boardList.map(BoardListResponse::fromEntity);
    }

    public BoardDetailResponse boardDetail(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("해당 게시글이 없습니다."));
        // Todo : comments도 한번에 보여주기
        return BoardDetailResponse.fromEntity(board);
    }
    @Transactional
    public BoardUpdateResponse updateBoard(Long boardId, BoardUpdateRequest dto)  {
        Board board = boardRepository.findById(boardId).orElseThrow(()-> new EntityNotFoundException("해당 게시글이 없습니다."));

        // 작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!board.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 게시글을 작성한 회원만 수정이 가능합니다.");
        board.updateBoard(dto);
        return BoardUpdateResponse.fromEntity(board);
    }

    public BoardDeleteResponse boardDelete(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("해당 게시글이 없습니다."));

        // 작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!board.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 게시글을 작성한 회원만 삭제가 가능합니다.");
        board.updateDelYn();
        return BoardDeleteResponse.fromEntity(board);
    }





}
