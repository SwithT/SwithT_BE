package com.tweety.SwithT.comment.controller;

import com.tweety.SwithT.comment.dto.create.CommentCreateRequest;
import com.tweety.SwithT.comment.dto.create.CommentCreateResponse;
import com.tweety.SwithT.comment.dto.delete.CommentDeleteResponse;
import com.tweety.SwithT.comment.dto.read.CommentListResponse;
import com.tweety.SwithT.comment.dto.update.CommentUpdateRequest;
import com.tweety.SwithT.comment.dto.update.CommentUpdateResponse;
import com.tweety.SwithT.comment.service.CommentService;
import com.tweety.SwithT.common.dto.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/board/{id}/comment/create")
    public ResponseEntity<SuccessResponse> commentCreate(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody CommentCreateRequest commentCreateRequest){
        CommentCreateResponse commentCreateResponse = commentService.commentCreate(request,id,commentCreateRequest);

        SuccessResponse response = SuccessResponse.builder()
                .httpStatus(HttpStatus.CREATED)
                .result(commentCreateRequest)
                .statusMessage("댓글이 등록되었습니다")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 댓글 목록 조회
    @GetMapping("/board/{id}/comment/list")
    public ResponseEntity<SuccessResponse> commentList(@PathVariable("id") Long id,@PageableDefault(size = 5) Pageable pageable){
        Page<CommentListResponse> commentListResponses = commentService.commentList(id, pageable);

        SuccessResponse response = SuccessResponse.builder()
                .statusMessage("댓글 목록 조회입니다.")
                .httpStatus(HttpStatus.OK)
                .result(commentListResponses)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 댓글 수정
    @PutMapping("/board/comment/{id}")
    public ResponseEntity<SuccessResponse> commentUpdate(@PathVariable("id") Long id,@RequestBody CommentUpdateRequest dto){
        CommentUpdateResponse commentUpdateResponse = commentService.commentUpdate(id,dto);

        SuccessResponse response = SuccessResponse.builder()
                .statusMessage("댓글 수정 됐습니다.")
                .httpStatus(HttpStatus.OK)
                .result(commentUpdateResponse)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 댓글 삭제
    @PatchMapping("/board/comment/{id}/delete")
    public ResponseEntity<SuccessResponse> commentDelete(@PathVariable("id") Long id){
        CommentDeleteResponse commentDeleteResponse = commentService.commentDelete(id);
        SuccessResponse response = SuccessResponse.builder()
                .httpStatus(HttpStatus.OK)
                .result(commentDeleteResponse)
                .statusMessage("댓글이 삭제되었습니다")
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
