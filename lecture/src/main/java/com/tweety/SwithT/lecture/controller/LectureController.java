package com.tweety.SwithT.lecture.controller;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture.domain.LectureType;
import com.tweety.SwithT.lecture.dto.LectureSearchDto;
import com.tweety.SwithT.lecture.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    //과외 또는 강의 리스트
    @GetMapping("/list-of-lecture")
    public ResponseEntity<?> showLectureList(@ModelAttribute LectureSearchDto searchDto, Pageable pageable){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 리스트", lectureService.showLectureList(searchDto, pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }

}
