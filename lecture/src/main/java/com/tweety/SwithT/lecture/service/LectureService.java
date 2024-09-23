package com.tweety.SwithT.lecture.service;

import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.dto.LectureGroupReqDto;
import com.tweety.SwithT.lecture.dto.LectureReqDto;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureGroupRepository lectureGroupRepository;

    public LectureService(LectureRepository lectureRepository, LectureGroupRepository lectureGroupRepository) {
        this.lectureRepository = lectureRepository;
        this.lectureGroupRepository = lectureGroupRepository;
    }

    // Create
    @Transactional
    public Lecture lectureCreate(Long memberId, LectureReqDto lectureReqDto, List<LectureGroupReqDto> lectureGroupReqDtos){
        Lecture createdLecture = lectureRepository.save(lectureReqDto.toEntity(memberId));
        for (LectureGroupReqDto groups : lectureGroupReqDtos){
            lectureGroupRepository.save(groups.toEntity(createdLecture));
        }
        return createdLecture;
    }

    // Read
    public Page<LectureReqDto> lectureList


    // Update

    // Delete
}
