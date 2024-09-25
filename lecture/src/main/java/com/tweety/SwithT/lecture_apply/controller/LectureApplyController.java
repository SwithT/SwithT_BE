package com.tweety.SwithT.lecture_apply.controller;

import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplySavedDto;
import com.tweety.SwithT.lecture_apply.service.LectureApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LectureApplyController {

    private final LectureApplyService lectureApplyService;


    //과외 신청
    @PreAuthorize("hasRole('TUTEE')")
    @PostMapping("/single-lecture-apply")
    public ResponseEntity<?> tuteeSingleLectureApply(@RequestBody SingleLectureApplySavedDto dto){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "튜티의 과외 신청 완료", lectureApplyService.tuteeSingleLectureApply(dto));
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);

    }

}
