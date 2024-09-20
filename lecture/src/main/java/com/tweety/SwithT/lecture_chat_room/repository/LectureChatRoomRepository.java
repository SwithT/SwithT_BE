package com.tweety.SwithT.lecture_chat_room.repository;

import com.tweety.SwithT.lecture_chat_room.domain.LectureChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureChatRoomRepository extends JpaRepository<LectureChatRoom, Long> {
}
