package com.tweety.SwithT.lecture_chat_room.controller;

import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture_chat_room.dto.ChatRoomCheckDto;
import com.tweety.SwithT.lecture_chat_room.dto.ChatRoomConnectDto;
import com.tweety.SwithT.lecture_chat_room.dto.SendMessageDto;
//import com.tweety.SwithT.lecture_chat_room.service.LectureChatRoomService;
import com.tweety.SwithT.lecture_chat_room.service.LectureChatRoomService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;

@RestController
@RequiredArgsConstructor
public class LectureChatRoomController {
    private final LectureChatRoomService lectureChatRoomService;
    private final SimpMessagingTemplate template;

    //튜티의 채팅하기 버튼(채팅방 번호만 가져옴. 연결 x)
    @PreAuthorize("hasRole('TUTEE')")
    @PostMapping("/tutee-room-check-or-create")
    public ResponseEntity<?> tuteeChatRoomCheckOrCreate(@RequestParam(value = "lectureGroupId")Long lectureGroupId){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방 확인", lectureChatRoomService.chatRoomCheckOrCreate(lectureGroupId));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //튜터의 과외 채팅하기 버튼(채팅방 번호만 가져옴. 연결 x)
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/tutor-lesson-chat-check-or-create")
    public ResponseEntity<?> tutorLessonChatCheckOrCreate(@RequestBody ChatRoomCheckDto dto){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "튜터 - 과외 채팅하기. 채팅방 확인", lectureChatRoomService.tutorLessonChatCheckOrCreate(dto));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //튜터의 강의 채팅하기 버튼(채팅방 번호만 가져옴. 연결 x)
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/tutor-lecture-chat-check/{id}")
    public ResponseEntity<?> tutorLectureChatCheck(@PathVariable(value = "id") Long lectureGroupId){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "튜터 - 강외 채팅하기. 채팅방 확인", lectureChatRoomService.tutorLectureChatCheck(lectureGroupId));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //채팅방 입장
    @MessageMapping("/room/{roomId}/entered")
    public void entered(@DestinationVariable(value = "roomId") String roomId){
        lectureChatRoomService.chatRoomEntered(roomId);

    }

    //채팅 전송
    @MessageMapping("/room/{roomId}")
    public void sendMessage(@DestinationVariable(value = "roomId") String roomId, @Payload SendMessageDto chatMessage) {
        lectureChatRoomService.chatSend(roomId, chatMessage.getMessage(), chatMessage.getMemberId(), chatMessage.getMemberName());
    }

    //채팅방 목록
    @GetMapping("/room/list")
    public ResponseEntity<?>  myRoomList(String chatRoomId){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "나의 채팅방 리스트", lectureChatRoomService.myChatRoomList(chatRoomId));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
