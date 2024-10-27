package com.tweety.SwithT.comment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.repository.BoardRepository;
import com.tweety.SwithT.comment.domain.Comment;
import com.tweety.SwithT.comment.dto.create.CommentCreateReqDto;
import com.tweety.SwithT.comment.dto.create.CommentCreateResDto;
import com.tweety.SwithT.comment.dto.delete.CommentDeleteResDto;
import com.tweety.SwithT.comment.dto.read.CommentListResDto;
import com.tweety.SwithT.comment.dto.update.CommentUpdateReqDto;
import com.tweety.SwithT.comment.dto.update.CommentUpdateResDto;
import com.tweety.SwithT.comment.repository.CommentRepository;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.dto.MemberNameResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public CommentCreateResDto commentCreate(Long id, CommentCreateReqDto dto){
        // securityContextHolder에서 member id 가져옴
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        // feign으로 member 이름 가져옴
        CommonResDto commonResDto = memberFeign.getMemberNameById(memberId);
        ObjectMapper objectMapper = new ObjectMapper();
        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
        String memberName = memberNameResDto.getName();

        Board board = boardRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("해당 게시글이 없습니다"));

        Comment comment = commentRepository.save(CommentCreateReqDto.toEntity(memberId,memberName,board, dto));
        return CommentCreateResDto.fromEntity(comment);
    }

    // 댓글 목록 조회
    public Page<CommentListResDto> commentList(Long id, Pageable pageable){
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdTime")
        );
        Page<Comment> comments = commentRepository.findByBoardIdAndDelYn(id,"N",sortedPageable);
        return comments.map(CommentListResDto::fromEntity);
    }

    // 댓글 수정
    @Transactional
    public CommentUpdateResDto commentUpdate(Long id, CommentUpdateReqDto dto){
        Comment comment = commentRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 댓글이 없습니다"));

        //작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!comment.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 댓글을 작성한 회원만 수정이 가능합니다.");

        comment.updateComment(dto);
        return CommentUpdateResDto.fromEntity(comment);
    }

    // 댓글 삭제
    public CommentDeleteResDto commentDelete(Long id){
        Comment comment = commentRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 댓글이 없습니다"));

        //작성자 확인
        Long loginMemberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if(!comment.getMemberId().equals(loginMemberId)) throw new RuntimeException("해당 댓글을 작성한 회원만 삭제 가능합니다.");

        comment.updateDelYn();
        Comment saveComment = commentRepository.save(comment);
        return CommentDeleteResDto.fromEntity(saveComment);
    }
}
