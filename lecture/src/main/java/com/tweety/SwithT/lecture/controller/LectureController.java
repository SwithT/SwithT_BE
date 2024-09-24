package com.tweety.SwithT.lecture.controller;

import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.dto.LectureGroupReqDto;
import com.tweety.SwithT.lecture.dto.LectureGroupResDto;
import com.tweety.SwithT.lecture.dto.LectureReqDto;
import com.tweety.SwithT.lecture.dto.LectureResDto;
import com.tweety.SwithT.lecture.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lecture")
public class LectureController {
    private final LectureService lectureService;

    // 강의 Or 과외 생성
    @PostMapping("/create")
    public ResponseEntity<Object> lectureCreate(@RequestBody LectureReqDto lectureReqDto, List<LectureGroupReqDto> lectureGroupReqDtos,  @AuthenticationPrincipal UserInfo userInfo) {
        Lecture lecture = lectureService.lectureCreate(userInfo.memberId, lectureReqDto, lectureGroupReqDtos);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "comment is successfully created", lecture.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 전체 강의 목록
    @GetMapping("/lectureList")
    public ResponseEntity<Object> lectureList(
            @PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.ASC) Pageable pageable
    ){
        Page<LectureGroupResDto> lectureGroupList = lectureService.lectureList(pageable);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Success", lectureGroupList), HttpStatus.OK);
    }

    // 전체 과외 목록
    @GetMapping("/lessonList")
    public ResponseEntity<Object> lessonList(
            @PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.ASC) Pageable pageable
    ){
        Page<LectureGroupResDto> lectureGroupList = lectureService.lessonList(pageable);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Success", lectureGroupList), HttpStatus.OK);
    }


}
