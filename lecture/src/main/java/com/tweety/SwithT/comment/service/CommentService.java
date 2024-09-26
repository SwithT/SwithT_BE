package com.tweety.SwithT.comment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.repository.BoardRepository;
import com.tweety.SwithT.comment.domain.Comment;
import com.tweety.SwithT.comment.dto.create.CommentCreateRequest;
import com.tweety.SwithT.comment.dto.create.CommentCreateResponse;
import com.tweety.SwithT.comment.dto.delete.CommentDeleteResponse;
import com.tweety.SwithT.comment.dto.read.CommentListResponse;
import com.tweety.SwithT.comment.dto.update.CommentUpdateRequest;
import com.tweety.SwithT.comment.dto.update.CommentUpdateResponse;
import com.tweety.SwithT.comment.repository.CommentRepository;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.dto.MemberNameResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;



@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberFeign memberFeign;
    @Value("${jwt.secretKey}")
    private String secretKey;
    public CommentService(CommentRepository commentRepository, BoardRepository boardRepository, MemberFeign memberFeign) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.memberFeign = memberFeign;
    }

    // 댓글 생성
    public CommentCreateResponse commentCreate( Long id, CommentCreateRequest dto){
        // securityContextHolder에서 member id 가져옴
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        // feign으로 member 이름 가져옴
        CommonResDto commonResDto = memberFeign.getMemberNameById(memberId);
        ObjectMapper objectMapper = new ObjectMapper();
        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
        String memberName = memberNameResDto.getName();

        Board board = boardRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("해당 게시글이 없습니다"));

        Comment comment = commentRepository.save(CommentCreateRequest.toEntity(memberId,memberName,board, dto));
        return CommentCreateResponse.fromEntity(comment);
    }

    // 댓글 목록 조회
    public Page<CommentListResponse> commentList(Long id, Pageable pageable){
        Page<Comment> comments = commentRepository.findAllByBoardId(id,pageable);
        return comments.map(CommentListResponse::fromEntity);
    }

    // 댓글 수정
    @Transactional
    public CommentUpdateResponse commentUpdate(Long id, CommentUpdateRequest dto){
        Comment comment = commentRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 댓글이 없습니다"));

        //작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!comment.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 댓글을 작성한 회원만 수정이 가능합니다.");

        comment.updateComment(dto);
        return CommentUpdateResponse.fromEntity(comment);
    }

    // 댓글 삭제
    public CommentDeleteResponse commentDelete(Long id){
        Comment comment = commentRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 댓글이 없습니다"));

        //작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!comment.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 댓글을 작성한 회원만 삭제 가능합니다.");

        comment.updateDelYn();
        return CommentDeleteResponse.fromEntity(comment);
    }
}
