package com.tweety.SwithT.board.dto.read;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BoardDetailResDto {
    private Long id;
    private Long memberId;
    private String memberName;
    private String title;
    private String contents;
    private Type type;
    private boolean isAuthor;


    public static BoardDetailResDto fromEntity(Board board, Long memberId){
        return BoardDetailResDto.builder()
                .id(board.getId())
                .contents(board.getContents())
                .title(board.getTitle())
                .type(board.getType())
                .memberId(board.getMemberId())
                .memberName(board.getMemberName())
                .isAuthor(board.getMemberId().equals(memberId))
                .build();
    }
}
