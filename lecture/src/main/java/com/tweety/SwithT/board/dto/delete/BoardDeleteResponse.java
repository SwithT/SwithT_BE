package com.tweety.SwithT.board.dto.delete;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BoardDeleteResponse {
    private Long id;
    private String memberName;
    private String title;
    private Type type;

    public static BoardDeleteResponse fromEntity(Board board){
        return BoardDeleteResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .type(board.getType())
                .memberName(board.getMemberName())
                .build();
    }
}
