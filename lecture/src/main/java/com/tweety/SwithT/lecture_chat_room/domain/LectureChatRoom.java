package com.tweety.SwithT.lecture_chat_room.domain;

import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lecture.domain.Lecture;
import jakarta.persistence.*;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LectureChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lecture_group_id")
    private LectureGroup lectureGroup;
}
