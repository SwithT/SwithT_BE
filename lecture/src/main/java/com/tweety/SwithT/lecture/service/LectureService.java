package com.tweety.SwithT.lecture.service;

import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.dto.*;
import com.tweety.SwithT.lecture.repository.GroupTimeRepository;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureGroupRepository lectureGroupRepository;
    private final GroupTimeRepository groupTimeRepository;


    // Create
    @Transactional
    public Lecture lectureCreate(LectureReqDto lectureReqDto, List<LectureGroupReqDto> lectureGroupReqDtos){
        Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        // Lecture 정보 저장
        Lecture createdLecture = lectureRepository.save(lectureReqDto.toEntity(memberId));

        for (LectureGroupReqDto groupDto : lectureGroupReqDtos){
            // Lecture Group 정보 저장
            LectureGroup createdGroup = lectureGroupRepository.save(groupDto.toEntity(createdLecture));
            System.out.println(createdGroup.getId());
            for (GroupTimeReqDto timeDto : groupDto.getGroupTimeReqDtos()){
                System.out.println(timeDto.getEndTime());
                System.out.println(timeDto.getStartTime());
                groupTimeRepository.save(timeDto.toEntity(createdGroup));
            }
        }

        return createdLecture;
    }


    // 내 강의/과외 목록


    // Update: role=TUTOR & limitPeople=0
    public void lectureUpdate(LectureUpdateReqDto dto){

    }

    // Delete: role=TUTOR & limitPeople=0


}
