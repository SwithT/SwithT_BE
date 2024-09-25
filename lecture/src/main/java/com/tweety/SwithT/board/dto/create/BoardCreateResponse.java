package com.tweety.SwithT.board.dto.create;

import com.tweety.SwithT.board.domain.Board;
import com.tweety.SwithT.board.domain.Type;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BoardCreateResponse{
    private Long id;
    private Long lectureGroupId;
    private Long memberId;
    private String memberName;
    private String title;
    private String contents;
    private Type type;

    public static BoardCreateResponse fromEntity(Board board){
        return BoardCreateResponse.builder()
                .id(board.getId())
                .lectureGroupId(board.getLectureGroup().getId())
                .contents(board.getContents())
                .title(board.getTitle())
                .type(board.getType())
                .memberId(board.getMemberId())
                .memberName(board.getMemberName())
                .build();
    }
}
