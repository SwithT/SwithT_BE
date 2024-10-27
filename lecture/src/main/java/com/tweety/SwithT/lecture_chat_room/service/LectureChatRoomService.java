package com.tweety.SwithT.lecture_chat_room.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.dto.MemberNameResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.domain.LectureType;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture_chat_room.domain.LectureChatLogs;
import com.tweety.SwithT.lecture_chat_room.domain.LectureChatParticipants;
import com.tweety.SwithT.lecture_chat_room.domain.LectureChatRoom;
import com.tweety.SwithT.lecture_chat_room.dto.ChatRoomCheckDto;
import com.tweety.SwithT.lecture_chat_room.dto.ChatRoomResDto;
import com.tweety.SwithT.lecture_chat_room.dto.MyChatRoomListResDto;
import com.tweety.SwithT.lecture_chat_room.dto.SendMessageDto;
import com.tweety.SwithT.lecture_chat_room.repository.LectureChatLogsRepository;
import com.tweety.SwithT.lecture_chat_room.repository.LectureChatParticipantsRepository;
import com.tweety.SwithT.lecture_chat_room.repository.LectureChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureChatRoomService {
    private final LectureChatRoomRepository chatRoomRepository;
    private final LectureGroupRepository lectureGroupRepository;
    private final LectureChatParticipantsRepository chatParticipantsRepository;
    private final SimpMessageSendingOperations sendingOperations;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimpMessagingTemplate template;
    private final MemberFeign memberFeign;
    private final LectureChatLogsRepository lectureChatLogsRepository;

    //튜티가 채팅하기 눌렀을 때 채팅방 가져오거나 없으면 생성하기
    @Transactional
    public ChatRoomResDto chatRoomCheckOrCreate(Long lectureGroupId) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        LectureGroup lectureGroup = lectureGroupRepository.findByIdAndDelYn(lectureGroupId, "N").orElseThrow(()->{
            throw new EntityNotFoundException("강의그룹을 찾을 수 없습니다.");
        });
        List<LectureChatRoom> lectureChatRoomList = chatRoomRepository.findByLectureGroupAndDelYn(lectureGroup, "N");
        for(LectureChatRoom chatRoom: lectureChatRoomList){
            Long roomId = chatRoom.getId();
            if(chatParticipantsRepository.findByLectureChatRoomAndMemberIdAndDelYn(chatRoom, memberId, "N").isPresent()){
                //채팅방 존재하면
                return ChatRoomResDto.builder()
                        .roomId(roomId)
                        .build();

            }
        }

        //채팅방 존재하지않음.
        //채팅방 생성
        LectureChatRoom newChatRoom = LectureChatRoom.builder()
                .lectureGroup(lectureGroup)
                .build();
        chatRoomRepository.save(newChatRoom);

        //튜티 참여자 추가
        LectureChatParticipants tutee = LectureChatParticipants.builder()
                .lectureChatRoom(newChatRoom)
                .memberId(memberId)
                .build();
        chatParticipantsRepository.save(tutee);

        //튜터 참가자 추가
        LectureChatParticipants tutor = LectureChatParticipants.builder()
                .lectureChatRoom(newChatRoom)
                .memberId(lectureGroup.getLecture().getMemberId())
                .build();

        chatParticipantsRepository.save(tutor);

        return ChatRoomResDto.builder()
                .roomId(newChatRoom.getId())
                .build();

    }

    // 튜터가 채팅하기 클릭 (과외)
    @Transactional
    public ChatRoomResDto tutorLessonChatCheckOrCreate(ChatRoomCheckDto dto) {
        LectureGroup lectureGroup = lectureGroupRepository.findByIdAndDelYn(dto.getLectureGroupId(), "N").orElseThrow(()->{
            throw new EntityNotFoundException("강의그룹을 찾을 수 없습니다.");
        });
//        승인 보내는 시점에서 tutorId가 admin의 id로 들어가서 주석 처리
//        Long tutorId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        List<LectureChatRoom> lectureChatRoomList = chatRoomRepository.findByLectureGroupAndDelYn(lectureGroup, "N");
        for(LectureChatRoom chatRoom: lectureChatRoomList){
            if(chatParticipantsRepository.findByLectureChatRoomAndMemberIdAndDelYn(chatRoom, dto.getTuteeId(), "N").isPresent()){
                //채팅방 존재
                return ChatRoomResDto.builder()
                        .roomId(chatRoom.getId())
                        .build();
            }
        }

        //채팅방 없는 경우
        //채팅방 생성
        LectureChatRoom newChatRoom = LectureChatRoom.builder()
                .lectureGroup(lectureGroup)
                .build();
        chatRoomRepository.save(newChatRoom);

        //튜티 참여자 추가
        LectureChatParticipants tutee = LectureChatParticipants.builder()
                .lectureChatRoom(newChatRoom)
                .memberId(dto.getTuteeId())
                .build();
        chatParticipantsRepository.save(tutee);

        //튜터 참가자 추가
        LectureChatParticipants tutor = LectureChatParticipants.builder()
                .lectureChatRoom(newChatRoom)
                .memberId(lectureGroup.getLecture().getMemberId())
                .build();

        chatParticipantsRepository.save(tutor);

        return ChatRoomResDto.builder()
                .roomId(newChatRoom.getId())
                .build();
    }

    //튜터가 강의 채팅방 있는지 확인하기
    public ChatRoomResDto tutorLectureChatCheck(Long lectureGroupId) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        LectureGroup lectureGroup = lectureGroupRepository.findByIdAndDelYn(lectureGroupId, "N").orElseThrow(()->{
            throw new EntityNotFoundException("강의그룹을 찾을 수 없습니다.");
        });
        if(lectureGroup.getLecture().getMemberId()!=memberId){
            throw new IllegalArgumentException("접근할 수 없는 채팅방입니다.");
        }

        if(chatRoomRepository.findByLectureGroupAndDelYn(lectureGroup, "N").isEmpty()){
            throw new IllegalArgumentException("접근할 수 없는 채팅방입니다.");
        }
        //강의는 강의 그룹당 채팅방 단 한개
        LectureChatRoom chatRoom = chatRoomRepository.findByLectureGroupAndDelYn(lectureGroup, "N").get(0);

        return ChatRoomResDto.builder()
                .roomId(chatRoom.getId())
                .build();
    }

    //채팅방 입장
    public void chatRoomEntered(String roomId) {
        System.out.println(roomId + "방에 입장했습니다.");
    }

    //채팅 전송
    @Transactional
    public void chatSend(String roomId, String message, Long memberId, String memberName) {
        System.out.println(memberId+"님이 채팅을 보냈습니다. : "+message);
//        kafkaTemplate.send("chat-"+roomId, message);
        SendMessageDto sendMessageDto = SendMessageDto.builder()
                .chatRoomId(Long.parseLong(roomId))
                .message(message)
                .memberId(memberId)
                .memberName(memberName)
                .build();
        try{
            kafkaTemplate.send("chat-topic", roomId, sendMessageDto);
        }catch (Exception e){
            e.printStackTrace();
        }

        LectureChatRoom lectureChatRoom = chatRoomRepository.findById(Long.parseLong(roomId)).orElseThrow(()->{
            throw new EntityNotFoundException("채팅방이 존재하지 않습니다.");
        });
        LectureChatLogs chatLogs = LectureChatLogs.builder()
                .lectureChatRoom(lectureChatRoom)
                .memberId(memberId)
                .memberName(memberName)
                .contents(message)
                .build();
        lectureChatLogsRepository.save(chatLogs);
    }

    @KafkaListener(topics = "chat-topic", groupId = "lecture-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumerChat(@Header(KafkaHeaders.RECEIVED_KEY) String chatRoomId, @Payload String msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SendMessageDto sendMessageDto = objectMapper.readValue(msg, SendMessageDto.class);
//            System.out.println("Received message for room " + chatRoomId + ": " + sendMessageDto.getMessage());
            template.convertAndSend("/topic/chat-" + chatRoomId, sendMessageDto);
        } catch (Exception e) {
            System.out.println("카프카 리스너 에러!!!!!"+e.getMessage()+"\n\n\n\n\n\n\n\n\n\n\n");
            throw new RuntimeException(e);
        }
    }


    //내 채팅방 리스트
    public List<MyChatRoomListResDto> myChatRoomList(String chatRoomId) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        List<LectureChatParticipants> lectureChatParticipantsList = new ArrayList<>();

        //특정 채팅방으로 입장하는 경우
        if(chatRoomId != null && !chatRoomId.isEmpty()){
            LectureChatParticipants selectedChatRoom = chatParticipantsRepository.findByLectureChatRoomIdAndMemberIdAndDelYn(Long.parseLong(chatRoomId), memberId, "N").orElseThrow(()->{
                throw new EntityNotFoundException("채팅방이 존재하지 않습니다.");
            });
            lectureChatParticipantsList.add(selectedChatRoom);
        }

        if(!lectureChatParticipantsList.isEmpty()){
            for(LectureChatParticipants participants: chatParticipantsRepository.findByMemberIdAndDelYn(memberId, "N")){
                if(lectureChatParticipantsList.get(0).getLectureChatRoom().getId() != participants.getLectureChatRoom().getId()){
                    lectureChatParticipantsList.add(participants);
                }
            }
        }else{
            lectureChatParticipantsList.addAll(chatParticipantsRepository.findByMemberIdAndDelYn(memberId, "N"));
        }
        List<MyChatRoomListResDto> myChatRoomListResDtoList = lectureChatParticipantsList.stream().map(a-> {
            //과외인 경우 상대 이름을 채팅 목록에 표시해주기
            String memberName = null;
            if(a.getLectureChatRoom().getLectureGroup().getLecture().getLectureType() == LectureType.LESSON){
                List<LectureChatParticipants> chatParticipantsList = chatParticipantsRepository.findByLectureChatRoom(a.getLectureChatRoom());
                for(LectureChatParticipants participants : chatParticipantsList){
                    if(participants.getMemberId() != memberId){
                        CommonResDto commonResDto = memberFeign.getMemberNameById(participants.getMemberId());
                        ObjectMapper objectMapper = new ObjectMapper();
                        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
                        memberName = memberNameResDto.getName();
                        break;
                    }
                }
            }
            return a.fromEntityToMyChatRoomListResDto(memberName);
        }).toList();


        return myChatRoomListResDtoList;
    }
}
