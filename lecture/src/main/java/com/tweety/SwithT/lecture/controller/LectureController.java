package com.tweety.SwithT.lecture.controller;

import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.dto.LectureGroupReqDto;
import com.tweety.SwithT.lecture.dto.LectureReqDto;
import com.tweety.SwithT.lecture.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lecture")
public class LectureController {
    private final LectureService lectureService;

    @PostMapping("/create")
    public ResponseEntity<Object> lectureCreate(@RequestBody LectureReqDto lectureReqDto, List<LectureGroupReqDto> lectureGroupReqDtos  @AuthenticationPrincipal UserInfo userInfo) {
        Lecture lecture = lectureService.lectureCreate(userInfo.memberId, lectureReqDto, lectureGroupReqDtos);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "comment is successfully created", lecture.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
