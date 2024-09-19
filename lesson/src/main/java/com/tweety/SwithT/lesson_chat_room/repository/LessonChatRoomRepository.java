package com.tweety.SwithT.lesson_chat_room.repository;

import com.tweety.SwithT.lesson_chat_room.domain.LessonChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonChatRoomRepository extends JpaRepository<LessonChatRoom, Long> {
}
