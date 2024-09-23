package com.tweety.SwithT.board.dto.create;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardCreateRequest {
    private Long memberId;
    private String memberName;
    private String title;
    private String contents;
    private Type type;

    public static Board toEntity(LectureGroup lectureGroup,BoardCreateRequest dto ){
        return Board.builder()
                .lectureGroup(lectureGroup)
                .contents(dto.getContents())
                .title(dto.getTitle())
                .type(dto.getType())
                .memberId(dto.getMemberId())
                .memberName(dto.getMemberName())
                .build();
    }
}
