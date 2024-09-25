package com.tweety.SwithT.lecture_apply.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.service.MemberFeign;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.dto.MemberNameResDto;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplyAfterResDto;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplySavedDto;
import com.tweety.SwithT.lecture_apply.repository.LectureApplyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;


import java.util.List;

@Service
public class LectureApplyService {
    private final LectureGroupRepository lectureGroupRepository;
    private final LectureApplyRepository lectureApplyRepository;
    private final LectureRepository lectureRepository;
    private final MemberFeign memberFeign;

    public LectureApplyService(LectureGroupRepository lectureGroupRepository, LectureApplyRepository lectureApplyRepository, LectureRepository lectureRepository, MemberFeign memberFeign) {
        this.lectureGroupRepository = lectureGroupRepository;
        this.lectureApplyRepository = lectureApplyRepository;
        this.lectureRepository = lectureRepository;
        this.memberFeign = memberFeign;
    }

    @Value("${jwt.secretKey}")
    private String secretKey;

    //튜티가 과외 신청
    @Transactional
    public SingleLectureApplyAfterResDto tuteeSingleLectureApply(SingleLectureApplySavedDto dto) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
//        String token = ((UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getCredentials().toString();
//        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
//        String memberName = (String) claims.get("name");
        CommonResDto commonResDto = memberFeign.getMemberNameById(memberId);
        ObjectMapper objectMapper = new ObjectMapper();
        MemberNameResDto memberNameResDto = objectMapper.convertValue(commonResDto.getResult(), MemberNameResDto.class);
        String memberName = memberNameResDto.getName();


        LectureGroup lectureGroup = lectureGroupRepository.findById(dto.getLectureGroupId()).orElseThrow(()->{
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

        Lecture lecture = lectureRepository.findById(lectureGroup.getLecture().getId()).orElseThrow(()->{
            throw new EntityNotFoundException("해당 과외가 존재하지 않습니다.");
        });

        return SingleLectureApplyAfterResDto.builder().lectureTitle(lecture.getTitle()).build();


    }
}
