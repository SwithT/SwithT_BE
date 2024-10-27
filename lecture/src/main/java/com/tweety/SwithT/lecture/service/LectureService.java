package com.tweety.SwithT.lecture.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.dto.MemberNameResDto;
import com.tweety.SwithT.common.dto.MemberScoreResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import com.tweety.SwithT.common.service.OpenSearchService;
import com.tweety.SwithT.common.service.RedisStreamProducer;
import com.tweety.SwithT.common.service.S3Service;
import com.tweety.SwithT.lecture.domain.*;
import com.tweety.SwithT.lecture.dto.*;
import com.tweety.SwithT.lecture.repository.GroupTimeRepository;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import com.tweety.SwithT.lecture_apply.repository.LectureApplyRepository;
import com.tweety.SwithT.lecture_chat_room.domain.LectureChatRoom;
import com.tweety.SwithT.lecture_chat_room.dto.ChatRoomCheckDto;
import com.tweety.SwithT.lecture_chat_room.repository.LectureChatRoomRepository;
import com.tweety.SwithT.lecture_chat_room.service.LectureChatRoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LectureService {
    private final LectureRepository lectureRepository;
    private final LectureGroupRepository lectureGroupRepository;
    private final LectureChatRoomRepository lectureChatRoomRepository;
    private final GroupTimeRepository groupTimeRepository;
    private final LectureApplyRepository lectureApplyRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate kafkaTemplate;
    private final MemberFeign memberFeign;
    private final S3Service s3Service;
    private final OpenSearchService openSearchService;
    private final RedisStreamProducer redisStreamProducer;
    private final LectureChatRoomService lectureChatRoomService;

    // Create
    @Transactional
    public Lecture lectureCreate(LectureCreateReqDto lectureCreateReqDto, List<LectureGroupReqDto> lectureGroupReqDtos, MultipartFile imgFile){
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        CommonResDto commonResDto = memberFeign.getMemberNameById(memberId);
        ObjectMapper objectMapper = new ObjectMapper();
        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
        String memberName = memberNameResDto.getName();

        String imageUrl = s3Service.uploadFile(imgFile, "lecture");

        // Lecture 정보 저장
        Lecture createdLecture = lectureRepository.save(lectureCreateReqDto.toEntity(memberId, memberName, imageUrl));

        for (LectureGroupReqDto groupDto : lectureGroupReqDtos){
            // Lecture Group 정보 저장
            LectureGroup createdGroup = lectureGroupRepository.save(groupDto.toEntity(createdLecture));
            for (GroupTimeReqDto timeDto : groupDto.getGroupTimeReqDtos()){
                groupTimeRepository.save(timeDto.toEntity(createdGroup));
            }
//            if (createdLecture.getLectureType()== LectureType.LECTURE){
//                // LectureGroup 생성시 강의 타입이 Lecture일 경우 대기열 생성
//                waitingService.createQueue(createdGroup.getId(), createdGroup.getLimitPeople());
//            }
        }

        // OpenSearch에 데이터 동기화
        try {
            openSearchService.registerLecture(createdLecture.fromEntityToLectureResDto());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return createdLecture;
    }


    // 강의 검색
//    public List<LectureDetailResDto> searchLectures(String keyword, Pageable pageable) {
//        try {
//            return openSearchService.searchLectures(keyword, pageable);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            return new ArrayList<>();  // 검색 실패 시 빈 리스트 반환
//        }
//    }

    public Page<LectureListResDto> showLectureListInOpenSearch(LectureSearchDto searchDto, Pageable pageable) {
        String keyword = searchDto.getSearchTitle(); // 검색 제목

        try {
            if(!searchDto.getCategory().isEmpty()){
                // OpenSearch에서 검색 수행
                List<LectureDetailResDto> searchResults = openSearchService.searchLecturesByCategory(searchDto, pageable);
//            System.out.println(searchResults.get(0));
                // 검색 결과를 LectureListResDto로 변환하여 페이지 객체로 반환
                List<LectureListResDto> lectureList = searchResults.stream()
//                    여기서 필요한 데이터 조립
                        .map(detail -> LectureListResDto.builder()
                                .id(detail.getId())
                                .title(detail.getTitle())
                                .memberName(detail.getMemberName())
                                .memberId(detail.getMemberId())
                                .image(detail.getImage())
                                .category(detail.getCategory())
                                .isContainsFree(isContainsFreeGroup(detail.getId()))
                                .build())
                        .collect(Collectors.toList());

                // PageImpl로 페이지네이션 적용하여 반환
                return new PageImpl<>(lectureList, pageable, searchResults.size());
            } else{
                List<LectureDetailResDto> searchResults = openSearchService.searchLectures(keyword, pageable, searchDto);
                // OpenSearch에서 검색 수행
//            System.out.println(searchResults.get(0));
                // 검색 결과를 LectureListResDto로 변환하여 페이지 객체로 반환
                List<LectureListResDto> lectureList = searchResults.stream()
//                    여기서 필요한 데이터 조립
                        .map(detail -> LectureListResDto.builder()
                                .id(detail.getId())
                                .title(detail.getTitle())
                                .memberName(detail.getMemberName())
                                .memberId(detail.getMemberId())
                                .image(detail.getImage())
                                .category(detail.getCategory())
                                .isContainsFree(isContainsFreeGroup(detail.getId()))
                                .build())
                        .collect(Collectors.toList());

                // PageImpl로 페이지네이션 적용하여 반환
                return new PageImpl<>(lectureList, pageable, searchResults.size());
            }
        } catch (IOException | InterruptedException e) {
            // 예외 발생 시 로그 출력 및 빈 페이지 반환
            throw new IllegalArgumentException(e);
        }
    }

//    그룹 중 하나라도 무료이면 재능 기부로 침.
    private Boolean isContainsFreeGroup(Long lectureId){
        List<LectureGroup> lectureGroups = lectureGroupRepository.findByLectureId(lectureId);
        for(LectureGroup lectureGroup: lectureGroups){
            if(lectureGroup.getPrice().equals(0)){
                return true;
            }
        }
        return false;
    }

    // 검색어 추천 메서드
    public List<String> getSuggestions(String keyword) throws IOException, InterruptedException {
        return openSearchService.getSuggestions(keyword);
    }

    public Page<LectureListResDto> showLectureList(LectureSearchDto searchDto, Pageable pageable) {
        Specification<Lecture> specification = new Specification<Lecture>() {
            @Override
            public Predicate toPredicate(Root<Lecture> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));

                if(searchDto.getSearchTitle() != null){
                    predicates.add(criteriaBuilder.like(root.get("title"), "%"+searchDto.getSearchTitle()+"%"));
                }
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+searchDto.getCategory()+"%"));
                }
                if(searchDto.getLectureType() != null){
                    predicates.add(criteriaBuilder.like(root.get("lectureType"), "%"+searchDto.getLectureType()+"%"));
                }
                if(searchDto.getStatus() != null){
                    predicates.add(criteriaBuilder.like(root.get("status"), "%"+searchDto.getStatus()+"%"));
                }

                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };
        Page<Lecture> lectures = lectureRepository.findAll(specification, pageable);

        return lectures.map(Lecture::fromEntityToLectureListResDto);
    }


    //튜터 - 자신의 강의 리스트
    public Page<LectureListResDto> showMyLectureList(LectureSearchDto searchDto, Pageable pageable) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Specification<Lecture> specification = new Specification<Lecture>() {
            @Override
            public Predicate toPredicate(Root<Lecture> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("memberId"), memberId));
                predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));

                if(searchDto.getSearchTitle() != null && !searchDto.getSearchTitle().isEmpty()){
                    predicates.add(criteriaBuilder.like(root.get("title"), "%"+searchDto.getSearchTitle()+"%"));
                }
                if(searchDto.getCategory() != null && !searchDto.getCategory().isEmpty()){
                    predicates.add(criteriaBuilder.like(root.get("category"), "%" + searchDto.getCategory() + "%"));
                }
                if (searchDto.getLectureType() != null && !searchDto.getLectureType().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("lectureType"), searchDto.getLectureType().equals("LESSON")? LectureType.LESSON:LectureType.LECTURE));
                }
                if (searchDto.getStatus() != null && !searchDto.getStatus().isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("status"), "%" + searchDto.getStatus() + "%"));
                }

                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };
        Page<Lecture> lectures = lectureRepository.findAll(specification, pageable);

        return lectures.map(Lecture::fromEntityToLectureListResDto);
    }

    //강의 상세 화면
    public LectureDetailResDto lectureDetail(Long id) {
        Lecture lecture = lectureRepository.findByIdAndDelYn(id, "N").orElseThrow(()->{
            throw new EntityNotFoundException("해당 id에 맞는 강의가 존재하지 않습니다.");
        });

        CommonResDto commonResDto = memberFeign.getMemberScoreById(lecture.getMemberId());
        ObjectMapper objectMapper = new ObjectMapper();
        MemberScoreResDto memberScoreResDto = objectMapper.convertValue(commonResDto.getResult(), MemberScoreResDto.class);
        BigDecimal avgScore = memberScoreResDto.getAvgScore();

        lecture.increaseCount();
        lectureRepository.save(lecture);
        return lecture.fromEntityToLectureDetailResDto(avgScore);
    }

    //강의 그룹 및 그룹 시간 조회
    public Page<LectureGroupListResDto> showLectureGroupList(Long id, String isAvailable, Pageable pageable) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Lecture lecture = lectureRepository.findByIdAndDelYn(id, "N").orElseThrow(()->{
            throw new EntityNotFoundException("해당 id에 맞는 강의/과외가 존재하지 않습니다.");
        });
        if(lecture.getMemberId() != memberId){
            throw new IllegalArgumentException("로그인한 유저는 해당 과외의 튜터가 아닙니다.");
        }

        Specification<LectureGroup> specification = new Specification<LectureGroup>() {
            @Override
            public Predicate toPredicate(Root<LectureGroup> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("lecture"), lecture));
                predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));

                if(isAvailable != null && !isAvailable.isEmpty()){
                    predicates.add(criteriaBuilder.equal(root.get("isAvailable"), isAvailable));
                }

                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };

        Page<LectureGroup> lectureGroups = lectureGroupRepository.findAll(specification, pageable);
        Page<LectureGroupListResDto> lectureGroupResDtos = lectureGroups.map((a)->{
            List<GroupTime> groupTimeList = groupTimeRepository.findByLectureGroupId(a.getId());
            StringBuilder groupTitle = new StringBuilder();
            for(GroupTime groupTime : groupTimeList){
                groupTitle.append(changeLectureDayKorean(groupTime.getLectureDay())+" "+groupTime.getStartTime()+"~"+groupTime.getEndTime()+"  /  ");
            }

            if (groupTitle.length() > 0) {
                groupTitle.setLength(groupTitle.length() - 5);
            }

            String memberName = null;
            LocalDate startDate = null;
            LocalDate endDate = null;
            if(isAvailable.equals("N") && a.getLimitPeople()==1){
                //진행중인 과외인 경우
                if(!lectureApplyRepository.findByLectureGroupAndStatusAndDelYn(a, Status.ADMIT, "N").isEmpty()){
                    LectureApply lectureApply = lectureApplyRepository.findByLectureGroupAndStatusAndDelYn(a, Status.ADMIT, "N").get(0);
                    memberName = lectureApply.getMemberName();
                    startDate = a.getStartDate();
                    endDate = a.getEndDate();
                }
            }
            int limitPeople = 0;
            if(a.getLecture().getLectureType() == LectureType.LECTURE){
                limitPeople = a.getLimitPeople();
                startDate = a.getStartDate();
                endDate = a.getEndDate();
            }


            return LectureGroupListResDto.builder()
                    .title(groupTitle.toString())
                    .lectureGroupId(a.getId())
                    .memberName(memberName)
                    .price(a.getPrice())
                    .limitPeople(limitPeople)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
        });

        return lectureGroupResDtos;
    }

    // 강의 수정
    @Transactional
    public LectureDetailResDto lectureUpdate(Long id, LectureUpdateReqDto dto, MultipartFile image){
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lecture is not found"));

//        if (dto.getTitle() != null) {
//            lecture.updateTitle(dto.getTitle());
//        }
//        if (dto.getContents() != null) {
//            lecture.updateContents(dto.getContents());
//        }
//        if (dto.getImage() != null) {
//            lecture.updateImage(dto.getImage());
//        }
//        if (dto.getCategory() != null) {
//            lecture.updateCategory(dto.getCategory());
//        }

        String imageUrl = s3Service.uploadFile(image, "lecture");
        lecture.updateLecture(dto, imageUrl);


//        // OpenSearch에 데이터 동기화
//        try {
//            openSearchService.registerLecture(lecture.fromEntityToLectureResDto());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return lecture.fromEntityToLectureResDto();
    }

    // 강의 삭제
    @Transactional
    public void lectureDelete(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new EntityNotFoundException("Lecture is not found"));

        List<LectureGroup> lectureGroups = lectureGroupRepository.findByLectureId(lectureId);

        for (LectureGroup group : lectureGroups) {
            List<LectureApply> lectureApplies = group.getLectureApplies();

            // LectureApply가 하나라도 존재한다면 삭제 불가
            if (!lectureApplies.isEmpty()) {
                throw new IllegalArgumentException("LectureApply가 존재하여 Lecture를 삭제할 수 없습니다.");
            }
        }

        // 모든 LectureGroup의 LectureApply가 없을 경우, Lecture와 각각의 LectureGroup 삭제
        lecture.updateDelYn();
        for (LectureGroup group : lectureGroups) {
            group.updateDelYn();
            // LectureGroup의 GroupTime 삭제
            for (GroupTime groupTime : group.getGroupTimes()){
                groupTime.updateDelYn();
            }
        }

        // OpenSearch에서 삭제
        try {
            openSearchService.deleteLecture(lectureId);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 강의 그룹 수정
    @Transactional
    public void lectureGroupUpdate(Long lectureGroupId, LectureGroupReqDto dto){
        LectureGroup lectureGroup = lectureGroupRepository.findById(lectureGroupId)
                .orElseThrow(()->new EntityNotFoundException("Lecture group is not found"));

        // LectureApply가 하나라도 존재한다면 수정 불가
        if (!lectureGroup.getLectureApplies().isEmpty()) {
            throw new IllegalArgumentException("LectureApply가 존재하여 Lecture를 수정할 수 없습니다.");
        }
        if (dto.getPrice() != null) {
            lectureGroup.updatePrice(dto.getPrice());
        }
        if (dto.getLimitPeople() != null) {
            lectureGroup.updateLimitPeople(dto.getLimitPeople());
        }
        if (dto.getAddress() != null) {
            lectureGroup.updateAddress(dto.getAddress());
        }
        if (dto.getDetailAddress() != null) {
            lectureGroup.updateDetailAddress(dto.getDetailAddress());
        }
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            lectureGroup.updateDate(dto.getStartDate(), dto.getEndDate());
        }
        if (dto.getGroupTimeReqDtos() != null){
            for (GroupTime groupTime : groupTimeRepository.findByLectureGroupId(lectureGroupId)){
                groupTime.updateDelYn();
            }
            for (GroupTimeReqDto timeDto : dto.getGroupTimeReqDtos()){
                groupTimeRepository.save(timeDto.toEntity(lectureGroup));
            }
        }
    }

    // 강의 그룹 삭제
    @Transactional
    public void lectureGroupDelete(Long lectureGroupId){
        LectureGroup lectureGroup = lectureGroupRepository.findById(lectureGroupId)
                .orElseThrow(()->new EntityNotFoundException("Lecture group is not found"));

        // LectureApply가 하나라도 존재한다면 삭제 불가
        if (!lectureGroup.getLectureApplies().isEmpty()) {
            throw new IllegalArgumentException("LectureApply가 존재하여 Lecture를 삭제할 수 없습니다.");
        }
        lectureGroup.updateDelYn();
        for (GroupTime groupTime : lectureGroup.getGroupTimes()){
            groupTime.updateDelYn();
        }
    }

//    @KafkaListener(topics = "lecture-status-update", groupId = "lecture-group",
//            containerFactory = "kafkaListenerContainerFactory")
//    public void lectureStatusUpdate(String message) throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        LectureStatusUpdateDto statusUpdateDto = objectMapper.convertValue(
//                message, LectureStatusUpdateDto.class);
//        Lecture lecture = lectureRepository.findById(statusUpdateDto.getLectureId()).orElseThrow(
//                ()-> new EntityNotFoundException("강의 정보 가져오기에 실패했습니다."));
//        lecture.updateStatus(statusUpdateDto);
//
//    }

    @Transactional
    public void updateLectureStatus(LectureStatusUpdateDto statusUpdateDto) {
        Lecture lecture = lectureRepository.findById(statusUpdateDto.getLectureId())
                .orElseThrow(() -> new EntityNotFoundException("강의 정보 가져오기에 실패했습니다."));

        // String으로 받은 status를 다시 Enum으로 변환
        Status newStatus = Status.valueOf(statusUpdateDto.getStatus().toUpperCase());

        // 강의 상태를 업데이트하고 저장
        lecture.updateStatus(newStatus);
        if(newStatus.equals(Status.ADMIT)){
            getGroupTimes(statusUpdateDto.getLectureId());
        }
        lectureRepository.save(lecture);
    }

    @KafkaListener(topics = "lecture-status-update", groupId = "lecture-status-update", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void lectureStatusUpdateFromKafka(String message) {
        try {
//            System.out.println("수신된 Kafka 메시지: " + message);

//            아래 코드 없으면 "{\"lectureId\":1,\"status\":\"ADMIT\"}" 이중 직렬화 되어있어 계속 에러 발생
            if (message.startsWith("\"") && message.endsWith("\"")) {
                // 이스케이프 문자와 이중 직렬화를 제거
                message = message.substring(1, message.length() - 1).replace("\\", "");
//                System.out.println("이중 직렬화 제거 후 메시지: " + message);
            }

            // JSON 메시지를 LectureStatusUpdateDto로 변환
            LectureStatusUpdateDto statusUpdateDto = objectMapper.readValue(message, LectureStatusUpdateDto.class);

            Lecture lecture = lectureRepository.findById(statusUpdateDto.getLectureId()).orElseThrow(
                    ()-> new EntityNotFoundException("상태 업데이트 중 문제가 발생했습니다."));
            if(lecture.getLectureType().equals(LectureType.LECTURE)){
                updateLectureStatus(statusUpdateDto);
                List<LectureGroup> lectureGroups = lecture.getLectureGroups();
                for(LectureGroup lectureGroup: lectureGroups){
                    ChatRoomCheckDto chatRoomCheckDto = ChatRoomCheckDto.builder()
                            .lectureGroupId(lectureGroup.getId())
                            .tuteeId(lecture.getMemberId())
                            .build();
                    lectureChatRoomService.tutorLessonChatCheckOrCreate(chatRoomCheckDto);
                }
            }
            updateLectureStatus(statusUpdateDto);
            redisStreamProducer.publishMessage(
                    lecture.getMemberId().toString(), "강의 승인", lecture.getTitle() + " 강의가 승인되었습니다.", "메롱");
            // 상태 업데이트
//            System.out.println("Kafka 메시지 처리 완료: " + statusUpdateDto);
        } catch (JsonProcessingException e) {
            System.err.println("Kafka 메시지 변환 중 오류 발생: " + e.getMessage());
        }
    }

    public List<GroupTimeResDto> getGroupTimes(Long lectureId){
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(
                ()-> new EntityNotFoundException("강의 정보 불러오기 실패"));
        List<LectureGroup> lectureGroups = lecture.getLectureGroups();

        List<GroupTimeResDto> groupTimesDto = new ArrayList<>();

        for (LectureGroup lectureGroup : lectureGroups) {
            for (GroupTime groupTime : lectureGroup.getGroupTimes()) {
                GroupTimeResDto groupTimeResDto = GroupTimeResDto.builder()
                        .memberId(lecture.getMemberId())
                        .groupTimeId(groupTime.getId())
                        .lectureGroupId(lectureGroup.getId())
                        .lectureType(lecture.getLectureType().toString())
                        .lectureDay(groupTime.getLectureDay().name())
                        .startTime(groupTime.getStartTime().toString())
                        .endTime(groupTime.getEndTime().toString())
                        // startDate가 null일 때 null 처리를 위한 조건 추가
                        .startDate(lectureGroup.getStartDate() != null ? lectureGroup.getStartDate().toString() : "No start date")
                        .endDate(lectureGroup.getEndDate() != null ? lectureGroup.getEndDate().toString() : "No end date")
                        .schedulerTitle(lectureGroup.getLecture().getTitle())
                        .alertYn('N')
                        .build();

                groupTimesDto.add(groupTimeResDto);
            }
        }

        try {
            String message = objectMapper.writeValueAsString(groupTimesDto);

            kafkaTemplate.send("schedule-update", message);  // JSON 문자열 전송

            System.out.println("Kafka 메시지 전송됨: " + message);
        } catch (JsonProcessingException e) {
            System.err.println("Kafka 메시지 변환 및 전송 실패: " + e.getMessage());
        }

        return groupTimesDto;
    }


    // 강의 최신순 10개 조회
    public List<LectureInfoListResDto> getLatestLectures() {
        Pageable pageable = PageRequest.of(0, 10); // 첫 페이지, 10개 가져오기
        List<Lecture> lectures = lectureRepository.findByDelYnOrderByCreatedTime(pageable);
        List<LectureInfoListResDto> lectureInfos = new ArrayList<>();
        for (Lecture lecture : lectures){
            lectureInfos.add(lecture.fromEntityToLectureInfoListResDto());
        }
        return lectureInfos;
    }

    // 무료 강의 10개 최신순 조회
    public List<LectureInfoListResDto> getFreeLectures(){
        Pageable pageable = PageRequest.of(0, 10); // 첫 페이지, 10개 가져오기
        List<Lecture> lectures = lectureRepository.findLecturesWithAvailableGroups(pageable);
        List<LectureInfoListResDto> lectureInfos = new ArrayList<>();
        for (Lecture lecture : lectures){
            lectureInfos.add(lecture.fromEntityToLectureInfoListResDto());
        }
        return lectureInfos;
    }

    public LectureHomeResDto LectureHomeInfoGet(Long lectureGroupId){
        // 강의 그룹 정보
        LectureGroup lectureGroup = lectureGroupRepository.findById(lectureGroupId).orElseThrow(()->new EntityNotFoundException("해당 그룹이 없습니다."));

        List<LectureApply> lectureApplyList = lectureApplyRepository.findByLectureGroupAndStatusAndDelYn(lectureGroup, Status.ADMIT, "N");
        Long loginMember = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        LectureHomeResDto dto = LectureHomeResDto.builder()
                .groupId(lectureGroup.getId())
                .limitPeople(lectureGroup.getLimitPeople())
                .address(lectureGroup.getAddress())
                .detailAddress(lectureGroup.getDetailAddress())
                .price(lectureGroup.getPrice())
                .startDate(lectureGroup.getStartDate())
                .endDate(lectureGroup.getEndDate())
                .build();
        // 강의 그룹의 강의 id -> 강의 정보 불러오기
        Lecture lecture = lectureRepository.findById(lectureGroup.getLecture().getId()).orElseThrow(()->new EntityNotFoundException("해당 강의 및 과외가 없습니다."));

        List<Long> memberList = lectureApplyList.stream()
                .map(LectureApply::getMemberId)
                .collect(Collectors.toList());
        memberList.add(lecture.getMemberId());

        if(!memberList.contains(loginMember)) throw new IllegalArgumentException("접근할 수 없는 강의 그룹입니다");

        dto.setLectureId(lecture.getId());
        dto.setTitle(lecture.getTitle());
        dto.setContents(lecture.getContents());
        dto.setImage(lecture.getImage());
        dto.setMemberId(lecture.getMemberId());
        dto.setMemberName(lecture.getMemberName());
        dto.setCategory(lecture.getCategory());
        dto.setLectureType(lecture.getLectureType());

        // 단체 채팅방
        List<LectureChatRoom> lectureChatRoomList = lectureChatRoomRepository.findByLectureGroupAndDelYn(lectureGroup,"N");
        // Todo 채팅방 id는 좀 더 생각 필요
        if(!lectureChatRoomList.isEmpty())dto.setChatRoomId(lectureChatRoomList.get(0).getId());
        // 강의 그룹 시간 list
        List<GroupTimeResDto> groupTimeResDtos = new ArrayList<>();
        int totalDayCount=0;
        int pastDayCount=0;
        LocalDate today = LocalDate.now();

        for(GroupTime groupTime: lectureGroup.getGroupTimes()){
            if (groupTime.getDelYn().equals("N") ) {
                GroupTimeResDto groupTimeResDto = GroupTimeResDto.builder()
                        .memberId(lecture.getMemberId())
                        .groupTimeId(groupTime.getId())
                        .lectureGroupId(lectureGroup.getId())
                        .lectureType(lecture.getLectureType().toString())
                        .lectureDay(groupTime.getLectureDay().name()) // MON, TUE, 등
                        .startTime(groupTime.getStartTime().toString()) // HH:mm
                        .endTime(groupTime.getEndTime().toString()) // HH:mm
                        .startDate(lectureGroup.getStartDate().toString()) // 강의 시작 날짜
                        .endDate(lectureGroup.getEndDate().toString()) // 강의 종료 날짜
                        .schedulerTitle(lectureGroup.getLecture().getTitle()) // 강의 제목을 일정 제목으로 설정
                        .alertYn('N') // 기본값 'N'
                        .build();
                groupTimeResDtos.add(groupTimeResDto);

                // 요일 수업 개수 계산 (모든 GroupTime의 요일을 합산)
                totalDayCount += countDaysBetweenDates(lectureGroup.getStartDate(), lectureGroup.getEndDate(), groupTime.getLectureDay());

                // 현재 날짜까지의 수업 개수 계산
                pastDayCount += countDaysBetweenDates(lectureGroup.getStartDate(), today, groupTime.getLectureDay());
            }

        }
        groupTimeResDtos.sort(Comparator.comparing((GroupTimeResDto g) -> DayOfWeek.valueOf(g.getLectureDay()).ordinal())
                .thenComparing(g -> LocalTime.parse(g.getStartTime())));

        dto.setTotalDayCount(totalDayCount);
        dto.setPastDayCount(pastDayCount);
        dto.setLectureGroupTimes(groupTimeResDtos);
        return dto;
    }
    public int countDaysBetweenDates(LocalDate startDate, LocalDate endDate, LectureDay lectureDay) {
        DayOfWeek targetDayOfWeek = DayOfWeek.valueOf(lectureDay.name());

        int count = 0;
        LocalDate date = startDate;

        // 시작일부터 종료일까지 날짜 순회
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek() == targetDayOfWeek) {
                count++;
            }
            date = date.plusDays(1);
        }

        return count;
    }

    public LectureTitleAndImageResDto getTitleAndThumbnail(Long id){
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("강의 정보 가져오기에 실패했습니다."));
        return LectureTitleAndImageResDto.builder()
                .title(lecture.getTitle())
                .image(lecture.getImage())
                .build();
    }

    public LectureTitleAndImageResDto getTitleAndThumbnailByGroupId(Long id){
        LectureGroup lectureGroup = lectureGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("강의 정보 가져오기에 실패했습니다."));
        Lecture lecture = lectureGroup.getLecture();

        return LectureTitleAndImageResDto.builder()
                .title(lecture.getTitle())
                .image(lecture.getImage())
                .build();
    }

    public LectureGroupResDto getLectureGroupInfo(Long id){
        LectureGroup lectureGroup = lectureGroupRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("강의 그룹 가져오기 실패"));
        List<LectureGroupTimeResDto> timeResDtos = new ArrayList<>();
        List<GroupTime> groupTimes = lectureGroup.getGroupTimes();
        for(GroupTime groupTime : groupTimes){
            LectureGroupTimeResDto dto = LectureGroupTimeResDto.builder()
                    .lectureDay(groupTime.getLectureDay().toString())
                    .startTime(groupTime.getStartTime().toString())
                    .endTime(groupTime.getEndTime().toString())
                    .build();
            timeResDtos.add(dto);
        }
        return LectureGroupResDto.builder()
                .title(lectureGroup.getLecture().getTitle())
                .image(lectureGroup.getLecture().getImage())
                .address(lectureGroup.getAddress())
                .detailAddress(lectureGroup.getDetailAddress())
                .times(timeResDtos)
                .remaining(lectureGroup.getRemaining())
                .tutorName(lectureGroup.getLecture().getMemberName())
                .category(lectureGroup.getLecture().getCategory().name())
                .build();
    }


    // 해당 강의의 강의 그룹들 정보 조회
    public List<LectureGroupsResDto> getLectureGroupsInfo(Long lectureId){
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(
                () -> new EntityNotFoundException("강의 정보 가져오기 실패"));

        List<LectureGroup> lectureGroups = lectureGroupRepository.findByLectureIdAndDelYn(lectureId, "N");
        List<LectureGroupsResDto> lectureGroupsResDtos = new ArrayList<>();

        for (LectureGroup lectureGroup : lectureGroups) {
            List<GroupTimesResDto> groupTimesResDtos = new ArrayList<>();
           for (GroupTime groupTime : lectureGroup.getGroupTimes()) {
               groupTimesResDtos.add(
                       GroupTimesResDto.builder()
                               .groupTimeId(groupTime.getId())
                               .lectureDay(groupTime.getLectureDay())
                               .startTime(groupTime.getStartTime())
                               .endTime(groupTime.getEndTime())
                               .build()
               );
           }
            LectureGroupsResDto dto = LectureGroupsResDto.builder()
                    .lectureGroupId(lectureGroup.getId())
                    .groupTimes(groupTimesResDtos)
                    .isAvailable(lectureGroup.getIsAvailable())
                    .remaining(lectureGroup.getRemaining())
                    .price(lectureGroup.getPrice())
                    .address(lectureGroup.getAddress())
                    .detailAddress(lectureGroup.getDetailAddress())
                    .startDate(lectureGroup.getStartDate())
                    .endDate(lectureGroup.getEndDate())
                    .build();

            lectureGroupsResDtos.add(dto);
        }

        return lectureGroupsResDtos;
    }

    public String changeLectureDayKorean(LectureDay lectureDay){
        return switch (lectureDay) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

}
