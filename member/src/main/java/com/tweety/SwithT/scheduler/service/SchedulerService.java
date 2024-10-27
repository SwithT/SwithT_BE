package com.tweety.SwithT.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.repository.MemberRepository;
import com.tweety.SwithT.scheduler.domain.ScheduleAlert;
import com.tweety.SwithT.scheduler.domain.Scheduler;
import com.tweety.SwithT.scheduler.dto.*;
import com.tweety.SwithT.scheduler.repository.SchedulerAlertRepository;
import com.tweety.SwithT.scheduler.repository.SchedulerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SchedulerService {

    private final SchedulerRepository schedulerRepository;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final SchedulerAlertRepository schedulerAlertRepository;
    private final SchedulerAlertService schedulerAlertService;

    public SchedulerService(SchedulerRepository schedulerRepository, ObjectMapper objectMapper, MemberRepository memberRepository, SchedulerAlertRepository schedulerAlertRepository, SchedulerAlertService schedulerAlertService) {
        this.schedulerRepository = schedulerRepository;
        this.objectMapper = objectMapper;
        this.memberRepository = memberRepository;
        this.schedulerAlertRepository = schedulerAlertRepository;
        this.schedulerAlertService = schedulerAlertService;
    }

    @KafkaListener(topics = "schedule-update", groupId = "member-group-schedule-update", containerFactory = "kafkaListenerContainerFactory")
    public void updateScheduleFromKafka(String message) {
        try {
//            System.out.println("수신된 Kafka 메시지: " + message);

//            아래 코드 없으면 "{\"lectureId\":1,\"status\":\"ADMIT\"}" 이중 직렬화 되어있어 계속 에러 발생
            if (message.startsWith("\"") && message.endsWith("\"")) {
                // 이스케이프 문자와 이중 직렬화를 제거
                message = message.substring(1, message.length() - 1).replace("\\", "");
//                System.out.println("이중 직렬화 제거 후 메시지: " + message);
            }
            List<GroupTimeResDto> groupTimeResDtos = objectMapper.readValue(
                    message,
                    new TypeReference<>() {
                    } // 리스트 형태로 변환
            );

            Member member = memberRepository.findById(groupTimeResDtos.get(0).getMemberId()).orElseThrow(
                    () -> new EntityNotFoundException("존재하지 않는 회원입니다."));

            // 각 GroupTimeResDto를 처리하는 로직
            for (GroupTimeResDto groupTimeResDto : groupTimeResDtos) {
                LocalDate startDate = LocalDate.parse(groupTimeResDto.getStartDate());
                LocalDate endDate = LocalDate.parse(groupTimeResDto.getEndDate());

                // GroupTimeResDto에 있는 요일 정보 (MON, TUE 등)를 DayOfWeek로 변환
                DayOfWeek lectureDay = DayOfWeek.valueOf(groupTimeResDto.getLectureDay().toUpperCase());

                List<Scheduler> schedulers = new ArrayList<>();  // 스케줄러 리스트를 생성

                // startDate부터 endDate까지 날짜를 반복하면서 요일을 확인
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if (date.getDayOfWeek().equals(lectureDay)) {
                        // 요일이 일치하면 스케줄러 생성
                        Scheduler scheduler = Scheduler.builder()
                                .title(groupTimeResDto.getSchedulerTitle())
                                .schedulerDate(date)
                                .schedulerTime(LocalTime.parse(groupTimeResDto.getStartTime()))  // 강의 시작 시간 설정
                                .content(groupTimeResDto.getSchedulerTitle() + "가 있는 날입니다.")
                                .alertYn(groupTimeResDto.getAlertYn())
                                .member(member)
                                .lectureGroupId(groupTimeResDto.getLectureGroupId())
                                .build();

                        schedulers.add(scheduler);
                    }
                }
                schedulerRepository.saveAll(schedulers);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Kafka 메시지 변환 중 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lecture 상태 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    public Scheduler addSchedule(ScheduleCreateDto dto){

        Member member = memberRepository.findById(
                Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(
                ()-> new EntityNotFoundException("존재하지 않는 회원 정보입니다."));

        Scheduler scheduler = dto.toEntity(member);
        schedulerRepository.save(scheduler);

        return scheduler;
    }

    public void updateSchedule(Long schedulerId, ScheduleUpdateDto dto){
        Member member = memberRepository.findById(
                Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(
                ()-> new EntityNotFoundException("존재하지 않는 회원 정보입니다."));

        Scheduler scheduler = schedulerRepository.findById(schedulerId).orElseThrow(
                ()-> new EntityNotFoundException("스케줄 정보 불러오기에 실패했습니다"));

        if(scheduler.getLectureAssignmentId()==null && scheduler.getLectureGroupId()==null){
            scheduler.updateSchedule(dto);

            schedulerRepository.save(scheduler);
        } else{
            throw new IllegalArgumentException("해당 스케줄은 수정할 수 없습니다.");
        }
    }

    public void deleteSchedule(Long schedulerId){
        Member member = memberRepository.findById(
                Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(
                ()-> new EntityNotFoundException("존재하지 않는 회원 정보입니다."));

        Scheduler scheduler = schedulerRepository.findById(schedulerId).orElseThrow(
                ()-> new EntityNotFoundException("스케줄 정보 불러오기에 실패했습니다"));
        if(!scheduler.getMember().equals(member)){
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        if(scheduler.getLectureGroupId() != null){
            throw new IllegalStateException("수업 일정은 삭제할 수 없습니다.");
        } else if(scheduler.getLectureAssignmentId() != null){
            throw new IllegalStateException("과제 일정은 삭제할 수 없습니다.");
        } else{
            scheduler.deleteSchedule();
            ScheduleAlert scheduleAlert = schedulerAlertRepository.findBySchedulerId(schedulerId);
            if(scheduleAlert!= null){
                schedulerAlertService.cancelAlert(scheduleAlert.getId());
            }
            schedulerRepository.save(scheduler);
        }
    }

    // 과제 생성 관련 kafka 리스터 추가
    @KafkaListener(topics = "assignment-create", groupId = "lecture-group-assignment-create", containerFactory = "kafkaListenerContainerFactory")
    public void handleAssignmentCreateMessage(String message) {
        try {
//            System.out.println("Kafka 메시지 수신됨: " + message);
//            String jsonString = "{\\\"tutorId\\\":3,\\\"tuteeList\\\":[2],\\\"assignmentId\\\":28,\\\"lectureGroupId\\\":1,\\\"title\\\":\\\"test3\\\",\\\"contents\\\":\\\"test3\\\",\\\"schedulerDate\\\":\\\"2024-10-01\\\",\\\"schedulerTime\\\":\\\"09:00:00\\\"}";
//            System.out.println(jsonString.equals(message));
            if (message.startsWith("\"") && message.endsWith("\"")) {
                // 이중 직렬화로 인해 앞뒤의 쿼트와 역슬래시만 제거
                message = message.substring(1, message.length() - 1).replace("\\\"", "\"");
            }
//            message = message.replaceAll("[\\\\\"]", "");
            System.out.println(message);
            AssignmentCreateReqDto dto = objectMapper.readValue(message, AssignmentCreateReqDto.class);
            System.out.println(dto);
            // 튜터 일정 생성
            createSchedulerForMember(dto.getTutorId(), dto);
            // 튜티들 일정 생성
            for (Long tuteeId : dto.getTuteeList()) {
                createSchedulerForMember(tuteeId, dto);
            }

        } catch (JsonProcessingException e) {
            System.err.println("Kafka 메시지 변환 중 오류 발생: " + e.getMessage());
        }
    }

    // 과제 스케줄 생성 메서드
    private void createSchedulerForMember(Long memberId, AssignmentCreateReqDto dto) {
        // member 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원은 존재하지 않습니다."));

        // 스케줄러 엔티티 생성 및 저장
        Scheduler scheduler = Scheduler.builder()
                .title(dto.getTitle())
                .lectureAssignmentId(dto.getAssignmentId())
                .schedulerDate(LocalDate.parse(dto.getSchedulerDate()))
                .schedulerTime(LocalTime.parse(dto.getSchedulerTime()))
                .content(dto.getContents())
                .member(member)
                .lectureGroupId(dto.getLectureGroupId())
                .build();

        schedulerRepository.save(scheduler);
        System.out.println("스케줄 저장 완료: " + scheduler.getTitle() + " - " + member.getId());
    }

    // 과제 수정 관련 kafka 리스너 추가 및 스케쥴 수정
    @KafkaListener(topics = "assignment-update", groupId = "lecture-group-assignment-update", containerFactory = "kafkaListenerContainerFactory")
    public void assignmentUpdate(String message) {
        try {
            System.out.println("Kafka 메시지 수신됨: " + message);
            if (message.startsWith("\"") && message.endsWith("\"")) {
                // 이중 직렬화로 인해 앞뒤의 쿼트와 역슬래시만 제거
                message = message.substring(1, message.length() - 1).replace("\\\"", "\"");
            }
            // update용 dto..?
            AssignmentUpdateReqDto dto = objectMapper.readValue(message, AssignmentUpdateReqDto.class);
            System.out.println(dto);

            // 튜터 일정 생성
            Member member = memberRepository.findById(dto.getTutorId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 회원은 존재하지 않습니다."));

            // 스케줄러 엔티티 찾기 -> assignmentId로 찾은 뒤 수정 내용 적용
            List<Scheduler> updateSchedulerList = schedulerRepository.findByLectureAssignmentId(dto.getAssignmentId());
            for(Scheduler scheduler : updateSchedulerList){
                scheduler.updateSchedule(dto);
                schedulerRepository.save(scheduler);
                System.out.println("스케줄 저장 완료: " + scheduler.getTitle() + " - " + member.getId());
            }

        } catch (JsonProcessingException e) {
            System.err.println("Kafka 메시지 변환 중 오류 발생: " + e.getMessage());
        }
    }

// 과제 삭제 카프카 리스너 추가 및 assignment id 기준으로 삭제
    @KafkaListener(topics = "assignment-delete", groupId = "lecture-group-assignment-delete", containerFactory = "kafkaListenerContainerFactory")
    public void assignmentDelete(String message) {
        try {
            System.out.println("Kafka 메시지 수신됨: " + message);
            if (message.startsWith("\"") && message.endsWith("\"")) {
                // 이중 직렬화로 인해 앞뒤의 쿼트와 역슬래시만 제거
                message = message.substring(1, message.length() - 1).replace("\\\"", "\"");
            }
            // update용 dto..?
            AssignmentDeleteReqDto dto = objectMapper.readValue(message, AssignmentDeleteReqDto.class);
            System.out.println(dto);

            // 스케줄러 엔티티 찾기
            List<Scheduler> updateSchedulerList = schedulerRepository.findByLectureAssignmentId(dto.getAssignmentId());
            for(Scheduler scheduler : updateSchedulerList){
                scheduler.deleteSchedule();
                schedulerRepository.save(scheduler);
                System.out.println("스케줄 삭제 완료: " + scheduler.getTitle() +" - "+ scheduler.getMember().getId());
            }
        } catch (JsonProcessingException e) {
            System.err.println("Kafka 메시지 변환 중 오류 발생: " + e.getMessage());
        }
    }
    public ScheduleResDto getScheduleDetail(Long id){
        Member member = memberRepository.findById(
                Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(
                ()-> new EntityNotFoundException("존재하지 않는 회원 정보입니다."));

        Scheduler scheduler =  schedulerRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("스케줄 정보를 불러올 수 없습니다."));
        if(!scheduler.getMember().equals(member)){
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        } else return scheduler.fromEntity();
    }

    public List<ScheduleResDto> getMonthSchedule(LocalDate month) {
        Member member = memberRepository.findById(
                Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 회원 정보입니다."));

        LocalDate startOfMonth = month.withDayOfMonth(1);
        LocalDate endOfMonth = month.with(TemporalAdjusters.lastDayOfMonth());

        // delYn이 'N'인 스케줄만 가져오도록 수정
        List<Scheduler> monthlyScheduleList = schedulerRepository
                .findAllByMemberAndSchedulerDateBetweenAndDelYn(member, startOfMonth, endOfMonth, "N");

        // ScheduleResDto로 변환하여 반환
        return monthlyScheduleList.stream()
                .map(Scheduler::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @KafkaListener(topics = "schedule-cancel-update", groupId = "member-group-schedule-cancel-update", containerFactory = "kafkaListenerContainerFactory")
    public void deleteScheduleByRefund(Long lectureGroupId){
        List<Scheduler> schedulerList = schedulerRepository.findAllByLectureGroupId(lectureGroupId);

        for(Scheduler scheduler: schedulerList){
            scheduler.deleteSchedule();
            ScheduleAlert scheduleAlert = schedulerAlertRepository.findBySchedulerId(scheduler.getId());
            if(scheduleAlert!= null){
                schedulerAlertService.cancelAlert(scheduleAlert.getId());
            }
        }
    }
}