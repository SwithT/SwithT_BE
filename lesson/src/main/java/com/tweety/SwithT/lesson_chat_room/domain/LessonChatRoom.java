package com.tweety.SwithT.lesson_chat_room.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lesson_apply.domain.LessonApplyDetail;
import com.tweety.SwithT.lesson_apply.domain.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    private String chatRoomTitle;

    @Column(nullable = false)
    private Long tutorId;

    @Column(nullable = false)
    private Long tuteeId;





}
