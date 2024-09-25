package com.tweety.SwithT.comment.service;

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
    @Value("${jwt.secretKey}")
    private String secretKey;
    public CommentService(CommentRepository commentRepository, BoardRepository boardRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
    }

    // 댓글 생성
    public CommentCreateResponse commentCreate(HttpServletRequest request, Long id, CommentCreateRequest dto){
        System.out.println("push를 위한 print");
        // member 이름 claim에서 가져옴
        String bearerToken = request.getHeader("Authorization");
        String token = bearerToken.substring(7);
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        String memberName = claims.get("name",String.class);

        // member id SecurityContextHolder에서 받아옴
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails)principal;
        Long memberId = Long.valueOf(userDetails.getUsername());

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
        comment.updateComment(dto);
        return CommentUpdateResponse.fromEntity(comment);
    }

    // 댓글 삭제
    public CommentDeleteResponse commentDelete(Long id){
        Comment comment = commentRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 댓글이 없습니다"));
        comment.updateDelYn();
        return CommentDeleteResponse.fromEntity(comment);
    }
}
