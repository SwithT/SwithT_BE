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
public class BoardListResponse {
    private Long id;
    private String memberName;
    private String title;
    private Type type;

    public static BoardListResponse fromEntity(Board board){
        return BoardListResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .type(board.getType())
                .memberName(board.getMemberName())
                .build();
    }
}
