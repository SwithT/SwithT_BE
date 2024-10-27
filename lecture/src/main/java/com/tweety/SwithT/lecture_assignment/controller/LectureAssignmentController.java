package com.tweety.SwithT.lecture_assignment.controller;

import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture_assignment.dto.create.LectureAssignmentCreateReqDto;
import com.tweety.SwithT.lecture_assignment.dto.create.LectureAssignmentCreateResDto;
import com.tweety.SwithT.lecture_assignment.dto.read.LectureAssignmentDetailResDto;
import com.tweety.SwithT.lecture_assignment.dto.read.LectureAssignmentListResDto;
import com.tweety.SwithT.lecture_assignment.dto.update.LectureAssignmentUpdateReqDto;
import com.tweety.SwithT.lecture_assignment.dto.update.LectureAssignmentUpdateResDto;
import com.tweety.SwithT.lecture_assignment.service.LectureAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LectureAssignmentController {
    private final LectureAssignmentService lectureAssignmentService;
    // 생성
    @PostMapping("/lecture/{lectureGroupId}/assignment/create")
    public ResponseEntity<CommonResDto> createAssignment(@PathVariable("lectureGroupId") Long lectureGroupId, @RequestBody LectureAssignmentCreateReqDto lectureAssignmentCreateReqDto){
        LectureAssignmentCreateResDto lectureAssignmentCreateResDto = lectureAssignmentService.assignmentCreate(lectureGroupId, lectureAssignmentCreateReqDto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED,"과제가 생성되었습니다.", lectureAssignmentCreateResDto),HttpStatus.CREATED);
    }


    // 목록
    @GetMapping("/lecture/{lectureGroupId}/assignment")
    public ResponseEntity<CommonResDto> assignmentList(@PathVariable("lectureGroupId") Long lectureGroupId, Pageable pageable, @RequestParam(required = false, name = "isDashBoard")String isDashBoard){
        Page<LectureAssignmentListResDto> lectureAssignmentListResponses = lectureAssignmentService.assignmentList(lectureGroupId,pageable, isDashBoard);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"과제 목록이 조회되었습니다.",lectureAssignmentListResponses),HttpStatus.OK);
    }
    // 상세
    @GetMapping("/lecture/assignment/{assignmentId}")
    public ResponseEntity<CommonResDto> assignmentDetail(@PathVariable("assignmentId") Long assignmentId){
        LectureAssignmentDetailResDto lectureAssignmentDetailResDto = lectureAssignmentService.assignmentDetail(assignmentId);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"과제 상세 조회되었습니다.", lectureAssignmentDetailResDto),HttpStatus.OK);
    }
    // 수정
    @PutMapping("/lecture/assignment/{lectureAssignmentId}/update")
    public ResponseEntity<CommonResDto> updateAssignment(@PathVariable("lectureAssignmentId") Long lectureAssignmentId, @RequestBody LectureAssignmentUpdateReqDto lectureAssignmentUpdateReqDto){
        LectureAssignmentUpdateResDto lectureAssignmentUpdateResDto = lectureAssignmentService.assignmentUpdate(lectureAssignmentId, lectureAssignmentUpdateReqDto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"과제가 수정되었습니다.", lectureAssignmentUpdateResDto),HttpStatus.OK);
    }

    // 삭제
    @PatchMapping("/lecture/assignment/{lectureAssignmentId}/delete")
    public ResponseEntity<CommonResDto> deleteAssignment(@PathVariable("lectureAssignmentId") Long lectureAssignmentId){
        String result = lectureAssignmentService.assignmentDelete(lectureAssignmentId);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"과제가 삭제되었습니다.",result),HttpStatus.OK);
    }
}
