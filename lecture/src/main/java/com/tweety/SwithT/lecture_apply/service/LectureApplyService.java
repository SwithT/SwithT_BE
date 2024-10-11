package com.tweety.SwithT.lecture_apply.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.dto.MemberNameResDto;
import com.tweety.SwithT.common.dto.MemberProfileResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import com.tweety.SwithT.common.service.RedisStreamProducer;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.dto.LectureGroupPayResDto;
import com.tweety.SwithT.lecture.dto.TuteeMyLectureListResDto;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplyAfterResDto;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplyListDto;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplySavedDto;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureTuteeListDto;
import com.tweety.SwithT.lecture_apply.dto.*;
import com.tweety.SwithT.lecture_apply.repository.LectureApplyRepository;
import com.tweety.SwithT.lecture_chat_room.domain.LectureChatRoom;
import com.tweety.SwithT.lecture_chat_room.repository.LectureChatParticipantsRepository;
import com.tweety.SwithT.lecture_chat_room.repository.LectureChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class LectureApplyService {
    private final LectureGroupRepository lectureGroupRepository;
    private final LectureApplyRepository lectureApplyRepository;
    private final LectureChatRoomRepository lectureChatRoomRepository;
    private final LectureChatParticipantsRepository lectureChatParticipantsRepository;
    private final LectureRepository lectureRepository;
    private final MemberFeign memberFeign;
    private final RedisStreamProducer redisStreamProducer;

    @Qualifier("5")
    private final RedisTemplate<String, Object> redisTemplate;


    @Value("${jwt.secretKey}")
    private String secretKey;

    private final Object lock = new Object();


    //튜티가 과외 신청
    @Transactional
    public SingleLectureApplyAfterResDto tuteeSingleLectureApply(SingleLectureApplySavedDto dto) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        CommonResDto commonResDto = memberFeign.getMemberNameById(memberId);
        ObjectMapper objectMapper = new ObjectMapper();
        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
        String memberName = memberNameResDto.getName();


        LectureGroup lectureGroup = lectureGroupRepository.findByIdAndDelYn(dto.getLectureGroupId(), "N").orElseThrow(()->{
            throw new EntityNotFoundException("해당 과외는 존재하지 않습니다.");
        });
        if(lectureGroup.getIsAvailable().equals("N")){
            throw new RuntimeException("해당 과외는 신청할 수 없습니다.");
        }

        List<LectureApply> lectureApplyList = lectureApplyRepository.findByMemberIdAndLectureGroup(memberId, lectureGroup);
        if(!lectureApplyList.isEmpty()){
            int rejectedCount = 0;
            for(LectureApply lectureApply : lectureApplyList){
                if(lectureApply.getStatus() == Status.STANDBY){
                    throw new RuntimeException("이미 신청한 과외입니다.");
                }
                if(lectureApply.getStatus() == Status.REJECT){
                    rejectedCount++;
                    if(rejectedCount>=3){
                        throw new RuntimeException("해당 과외는 3회 이상 거절되어 신청할 수 없습니다.");
                    }
                }
            }
        }
        lectureApplyRepository.save(dto.toEntity(lectureGroup, memberId, memberName));

        Lecture lecture = lectureRepository.findByIdAndDelYn(lectureGroup.getLecture().getId(), "N").orElseThrow(()->{
            throw new EntityNotFoundException("해당 과외가 존재하지 않습니다.");
        });

        return SingleLectureApplyAfterResDto.builder().lectureTitle(lecture.getTitle()).build();


    }

    //튜터가 보는 강의그룹 신청자 리스트
    public Page<SingleLectureApplyListDto> singleLectureApplyList(Long id, Pageable pageable) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        LectureGroup lectureGroup = lectureGroupRepository.findByIdAndDelYn(id, "N").orElseThrow(()->{
            throw new EntityNotFoundException("해당 강의 그룹이 없습니다");
        });
        Lecture lecture = lectureGroup.getLecture();
        if(lecture.getMemberId() != memberId){  //소유자가 아닌 경우
            throw new IllegalArgumentException("접근할 수 없는 강의 그룹입니다");
        }
        List<LectureApply> lectureApplyList = lectureApplyRepository.findByLectureGroupAndStatusAndDelYn(lectureGroup, Status.WAITING, "N");
        List<LectureApply> lectureApplyStandbyList = lectureApplyRepository.findByLectureGroupAndStatusAndDelYn(lectureGroup, Status.STANDBY, "N");
        lectureApplyList.addAll(lectureApplyStandbyList);
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), lectureApplyList.size());
        Page<LectureApply> lectureApplyPage = new PageImpl<>(lectureApplyList.subList(start, end), pageRequest, lectureApplyList.size());
        Page<SingleLectureApplyListDto> result = lectureApplyPage.map(a->a.fromEntityToSingleLectureApplyListDto());
        for(SingleLectureApplyListDto dto : result){
            // chatroomlist
            List<LectureChatRoom> lectureChatRoomList = lectureChatRoomRepository.findByLectureGroupAndDelYn(lectureGroup,"N");
            for(LectureChatRoom chatRoom : lectureChatRoomList){
                Long roomId = chatRoom.getId();
                if(lectureChatParticipantsRepository.findByLectureChatRoomIdAndMemberIdAndDelYn(roomId,dto.getMemberId(),"N" ).isEmpty()){
                    dto.setChatRoomId(null);
                }
                else{
                    dto.setChatRoomId(roomId);
                }
            }
        }
        return result;
    }

    // 강의홈 튜티 리스트
    public Page<SingleLectureTuteeListDto> singleLectureTuteeList(Long id, Pageable pageable) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        LectureGroup lectureGroup = lectureGroupRepository.findByIdAndDelYn(id, "N").orElseThrow(()->{
            throw new EntityNotFoundException("해당 강의 그룹이 없습니다");
        });
        Lecture lecture = lectureGroup.getLecture();
        if(lecture.getMemberId() != memberId){  //소유자가 아닌 경우
            throw new IllegalArgumentException("접근할 수 없는 강의 그룹입니다");
        }
        List<LectureApply> lectureApplyList = lectureApplyRepository.findByLectureGroupAndStatusAndDelYn(lectureGroup, Status.ADMIT, "N");
//        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
//        int start = (int) pageRequest.getOffset();
//        int end = Math.min((start + pageRequest.getPageSize()), lectureApplyList.size());
//        Page<LectureApply> lectureApplyPage = new PageImpl<>(lectureApplyList.subList(start, end), pageRequest, lectureApplyList.size());
        List<SingleLectureTuteeListDto> dtoList = new ArrayList<>();
        for(LectureApply apply : lectureApplyList){
            CommonResDto commonResDto = memberFeign.getMemberProfileById(apply.getMemberId());
            ObjectMapper objectMapper = new ObjectMapper();
            MemberProfileResDto memberProfileResDto = objectMapper.convertValue(commonResDto.getResult(), MemberProfileResDto.class);
            String image = memberProfileResDto.getImage();
            SingleLectureTuteeListDto dto =  SingleLectureTuteeListDto.builder()
                    .tuteeName(apply.getMemberName())
                    .tuteeProfile(image)
                    .memberId(apply.getMemberId())
                    .build();
            dtoList.add(dto);
        }
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), dtoList.size());
        Page<SingleLectureTuteeListDto> result = new PageImpl<>(dtoList.subList(start, end), pageRequest, dtoList.size());
//        return lectureApplyPage.map(a->a.fromEntityToSingleLectureTuteeListDto());
        return result;
    }


    //튜터 - 튜티의 신청 승인
    @Transactional
    public String singleLecturePaymentRequest(Long id) {
        LectureApply lectureApply = lectureApplyRepository.findByIdAndDelYn(id, "N").orElseThrow(()->{
            throw new EntityNotFoundException("id에 맞는 수강을 찾을 수 없습니다.");
        });
        LectureGroup lectureGroup = lectureApply.getLectureGroup();

        if(!lectureApplyRepository.findByLectureGroupAndStatusAndDelYn(lectureGroup, Status.WAITING, "N").isEmpty()){
            throw new IllegalArgumentException("결제 대기 중인 튜티가 존재합니다.");
        }

        lectureApply.updateStatus(Status.WAITING);

        //결제 요청 보내기
        redisStreamProducer.publishMessage(lectureApply.getMemberId().toString(), "결제요청", lectureGroup.getLecture().getTitle()+"에서 결제 요청을 했습니다.", lectureApply.getId().toString());


        return "튜터가 해당 수강신청을 승인했습니다.";
    }

    //튜터 - 튜티의 신청 거절
    @Transactional
    public String singleLectureApplyReject(Long id) {
        LectureApply lectureApply = lectureApplyRepository.findByIdAndDelYn(id, "N").orElseThrow(()->{
            throw new EntityNotFoundException("id에 맞는 수강을 찾을 수 없습니다.");
        });

        lectureApply.updateStatus(Status.REJECT);
        return "튜터가 해당 수강신청을 거절했습니다.";

    }

    //튜티 - 내 강의 리스트
    public Page<TuteeMyLectureListResDto> myLectureList(String status, Pageable pageable) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        Specification<LectureApply> specification = new Specification<LectureApply>() {
            @Override
            public Predicate toPredicate(Root<LectureApply> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("memberId"), memberId));
                predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));

                if (status != null && !status.isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };
        Page<LectureApply> lectureApplyPage = lectureApplyRepository.findAll(specification, pageable);
        List<TuteeMyLectureListResDto> tuteeMyLectureListResDtos = new ArrayList<>();
        for(LectureApply lectureApply : lectureApplyPage){
            tuteeMyLectureListResDtos.add(TuteeMyLectureListResDto.builder()
                            .title(lectureApply.getLectureGroup().getLecture().getTitle())
                            .startDate(lectureApply.getStartDate())
                            .endDate(lectureApply.getEndDate())
                            .tutorName(lectureApply.getLectureGroup().getLecture().getMemberName())
                            .price(lectureApply.getLectureGroup().getPrice())
                            .applyId(lectureApply.getId())
                            .lectureGroupId(lectureApply.getLectureGroup().getId())
                            .createdTime(lectureApply.getCreatedTime())
                            .status(lectureApply.getStatus())
                    .build());
        }


        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), tuteeMyLectureListResDtos.size());
        return new PageImpl<>(tuteeMyLectureListResDtos.subList(start, end), pageRequest, tuteeMyLectureListResDtos.size());

    }

    // 강의 신청
    @Transactional
    public LectureApplyAfterResDto tuteeLectureApply(Long lectureGroupId, Long memberId, String memberName) throws InterruptedException {
        synchronized (lock) {
            LectureGroup lectureGroup = lectureGroupRepository.findByIdAndDelYn(lectureGroupId, "N")
                    .orElseThrow(() -> new EntityNotFoundException("해당 강의는 존재하지 않습니다."));

            if (lectureGroup.getIsAvailable().equals("N")) {
                throw new RuntimeException("해당 강의는 신청할 수 없습니다.");
            }

            List<LectureApply> lectureApplyList = lectureApplyRepository.findByMemberIdAndLectureGroup(memberId, lectureGroup);
            if (!lectureApplyList.isEmpty()) {
                throw new RuntimeException("이미 신청한 강의입니다.");
            }

            final long now = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(lectureGroupId.toString(), memberId, now);
            log.info("대기열에 추가 - {}번 유저 ({}초)", memberId, now);

            Set<Object> queue = redisTemplate.opsForZSet().range(lectureGroupId.toString(), 0, 0); // 앞에 있는 사람

            if (queue != null && !queue.isEmpty()) {
                // 앞에 있는 유저부터 큐에서 제거
                Object frontUser = queue.iterator().next();
//                System.out.println("queue에서 가장 앞에 있는 유저: " + frontUser);
                redisTemplate.opsForZSet().remove(lectureGroupId.toString(), frontUser);

                // 남은 자리수 감소 및 처리
                if (lectureGroup.getRemaining() > 0) {
                    lectureGroup.decreaseRemaining();
                    lectureGroupRepository.saveAndFlush(lectureGroup);

                    Long user = (frontUser instanceof Integer) ? ((Integer) frontUser).longValue() : (Long) frontUser;

                    // 대기열 상태 저장
                    lectureApplyRepository.save(LectureApply.builder()
                            .lectureGroup(lectureGroup)
                            .memberId(user)
                            .memberName(memberName)
                            .status(Status.STANDBY)
                            .build());

                    log.info("현재 대기열 상태 업데이트 완료.");
                } else {
                    throw new RuntimeException("남은 자리가 없습니다.");
                }
            }
            return LectureApplyAfterResDto.builder().lectureGroupId(lectureGroup.getId()).build();
        }
    }

        // 결제로 넘기기
        waitingService.processPayment(lectureGroup);

        LectureApply lectureApply = lectureApplyRepository.save(dto.toEntity(lectureGroup, memberId, memberName));

        return lectureGroup.getId()+"번 강의에 수강 신청되었습니다.";
    }

    public LectureGroupPayResDto getLectureGroupByApplyId(Long id){
        LectureApply lectureApply = lectureApplyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("수강 번호 불러오기 실패"));
        LectureGroup lectureGroup = lectureApply.getLectureGroup();

        return LectureGroupPayResDto.builder()
                .groupId(lectureGroup.getId())
                .lectureName(lectureGroup.getLecture().getTitle())
                .price(lectureGroup.getPrice())
                .build();
    }

    public void updateLectureApplyStatus(Long id, String message){
        LectureApply lectureApply = lectureApplyRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("수강 정보 불러오기 실패"));

        lectureApply.updatePaidStatus(message);
        if(message.equals("paid")){
            LectureGroup lectureGroup = lectureApply.getLectureGroup();
            lectureGroup.decreaseRemaining();
        }
        lectureApplyRepository.save(lectureApply);
    }
}
