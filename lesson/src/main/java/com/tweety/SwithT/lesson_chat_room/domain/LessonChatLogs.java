package com.tweety.SwithT.lesson_chat_room.domain;

import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lesson_apply.domain.LessonApply;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonChatLogs extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_chat_room_id")
    private LessonChatRoom lessonChatRoom;

    @CreationTimestamp
    private LocalDateTime loggingTime;

    @Column(nullable = false)
    private String contents;

    @Column(nullable = false)
    private MemberType memberType;

    @Column(nullable = false)
    private Long memberId;





}
