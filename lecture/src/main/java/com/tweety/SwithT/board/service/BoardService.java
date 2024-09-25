package com.tweety.SwithT.board.service;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.dto.create.BoardCreateRequest;
import com.tweety.SwithT.board.dto.create.BoardCreateResponse;
import com.tweety.SwithT.board.dto.delete.BoardDeleteResponse;
import com.tweety.SwithT.board.dto.read.BoardDetailResponse;
import com.tweety.SwithT.board.dto.read.BoardListResponse;
import com.tweety.SwithT.board.dto.update.BoardUpdateRequest;
import com.tweety.SwithT.board.dto.update.BoardUpdateResponse;
import com.tweety.SwithT.board.repository.BoardRepository;
import com.tweety.SwithT.common.auth.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final LectureGroupRepository lectureGroupRepository;
    public BoardService(BoardRepository boardRepository, JwtTokenProvider jwtTokenProvider, LectureGroupRepository lectureGroupRepository) {
        this.boardRepository = boardRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.lectureGroupRepository = lectureGroupRepository;
    }

    //create
    public BoardCreateResponse createBoard(HttpServletRequest request, Long lectureGroupId, BoardCreateRequest dto){
        // member 이름 claim에서 가져옴
        String bearerToken = request.getHeader("Authorization");
        String token = bearerToken.substring(7);
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        String memberName = claims.get("name",String.class);

        // member id SecurityContextHolder에서 받아옴
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails)principal;
        Long memberId = Long.valueOf(userDetails.getUsername());

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
    public BoardUpdateResponse updateBoard(Long boardId, BoardUpdateRequest dto){
        // Todo 멤버 작성했는지 체크

        Board board = boardRepository.findById(boardId).orElseThrow(()-> new EntityNotFoundException("해당 게시글이 없습니다."));
        board.updateBoard(dto);
        return BoardUpdateResponse.fromEntity(board);
    }

    public BoardDeleteResponse boardDelete(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("해당 게시글이 없습니다."));
        board.updateDelYn();
        return BoardDeleteResponse.fromEntity(board);
    }





}
