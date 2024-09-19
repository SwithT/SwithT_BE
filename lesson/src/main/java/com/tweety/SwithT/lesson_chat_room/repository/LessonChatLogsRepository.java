package com.tweety.SwithT.lesson_chat_room.repository;

import com.tweety.SwithT.lesson_chat_room.domain.LessonChatLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonChatLogsRepository extends JpaRepository<LessonChatLogs, Long> {
}
