package com.tweety.SwithT.board.dto.create;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardCreateRequest {
    private String title;
    private String contents;
    private Type type;

    public static Board toEntity(Long memberId, String memberName,LectureGroup lectureGroup,BoardCreateRequest dto ){
        return Board.builder()
                .lectureGroup(lectureGroup)
                .contents(dto.getContents())
                .title(dto.getTitle())
                .type(dto.getType())
                .memberId(memberId)
                .memberName(memberName)
                .build();
    }
}
