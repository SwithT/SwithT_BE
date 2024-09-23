package com.tweety.SwithT.board.domain;

import com.tweety.SwithT.board.dto.update.BoardUpdateRequest;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_group_id")
    private LectureGroup lectureGroup;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String contents;
    @Enumerated(EnumType.STRING)
    private Type type;


    public void updateBoard(BoardUpdateRequest dto) {
        this.title = dto.getTitle();
        this.contents = dto.getContents();
        this.type = dto.getType();
    }





}
