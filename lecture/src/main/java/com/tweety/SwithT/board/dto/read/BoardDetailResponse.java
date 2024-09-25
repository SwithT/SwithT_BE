package com.tweety.SwithT.board.dto.read;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BoardDetailResponse{
    private Long id;
    private Long memberId;
    private String memberName;
    private String title;
    private String contents;
    private Type type;

    public static BoardDetailResponse fromEntity(Board board){
        return BoardDetailResponse.builder()
                .id(board.getId())
                .contents(board.getContents())
                .title(board.getTitle())
                .type(board.getType())
                .memberId(board.getMemberId())
                .memberName(board.getMemberName())
                .build();
    }
}
