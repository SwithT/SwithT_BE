package com.tweety.SwithT.lecture.service;

import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.domain.LectureType;
import com.tweety.SwithT.lecture.dto.LectureGroupReqDto;
import com.tweety.SwithT.lecture.dto.LectureGroupResDto;
import com.tweety.SwithT.lecture.dto.LectureReqDto;
import com.tweety.SwithT.lecture.dto.LectureResDto;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureGroupRepository lectureGroupRepository;

    // Create
    @Transactional
    public Lecture lectureCreate(Long memberId, LectureReqDto lectureReqDto, List<LectureGroupReqDto> lectureGroupReqDtos){
        Lecture createdLecture = lectureRepository.save(lectureReqDto.toEntity(memberId));
        for (LectureGroupReqDto groups : lectureGroupReqDtos){
            lectureGroupRepository.save(groups.toEntity(createdLecture));
        }
        return createdLecture;
    }

    // 강의 Read
    public Page<LectureResDto> lectureList(Pageable pageable){
        Page<Lecture> lectures = lectureRepository.findByLectureType(pageable, LectureType.LECTURE);    // 수정 필요
        Page<LectureResDto> dtos = lectures.map(Lecture::listFromEntity);
        return dtos;
    }

    // 과외 Read(limit = 1인 lecture)
    public Page<LectureResDto> lessonList(Pageable pageable){
        Page<Lecture> lectures = lectureRepository.findByLectureType(pageable, LectureType.LESSON);
        Page<LectureResDto> dtos = lectures.map(Lecture::listFromEntity);
        return dtos;
    }

    // Update
    public void lectureUpdate(LectureUpdateReqDto dto){

    }

    // Delete
}
