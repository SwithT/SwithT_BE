package com.tweety.SwithT.comment.dto.create;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.comment.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateRequest {
//    private Long memberId;
//    private String memberName;
    private String contents;

    public static Comment toEntity(Long memberId, String memberName,Board board,CommentCreateRequest dto){
        return Comment.builder()
                .board(board)
                .memberId(memberId)
                .memberName(memberName)
                .contents(dto.getContents())
                .build();
    }
}
