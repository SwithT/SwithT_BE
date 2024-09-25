package com.tweety.SwithT.comment.dto.read;

import com.tweety.SwithT.comment.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CommentListResponse {
    private Long id;
    private String memberName;
    private String contents;

    public static CommentListResponse fromEntity(Comment comment){
        return CommentListResponse.builder()
                .id(comment.getId())
                .memberName(comment.getMemberName())
                .contents(comment.getContents())
                .build();
    }
}
