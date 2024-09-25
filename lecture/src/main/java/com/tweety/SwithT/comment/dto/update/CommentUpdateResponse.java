package com.tweety.SwithT.comment.dto.update;

import com.tweety.SwithT.comment.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CommentUpdateResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private String contents;

    public static CommentUpdateResponse fromEntity(Comment comment){
        return CommentUpdateResponse.builder()
                .id(comment.getId())
                .memberId(comment.getMemberId())
                .memberName(comment.getMemberName())
                .contents(comment.getContents())
                .build();
    }
}
