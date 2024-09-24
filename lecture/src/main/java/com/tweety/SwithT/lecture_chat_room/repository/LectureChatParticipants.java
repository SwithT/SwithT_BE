package com.tweety.SwithT.lecture_chat_room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureChatParticipants extends JpaRepository<LectureChatParticipants, Long> {
}
