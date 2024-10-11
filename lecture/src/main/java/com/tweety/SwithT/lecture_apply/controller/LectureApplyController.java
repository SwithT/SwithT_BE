package com.tweety.SwithT.lecture_apply.controller;

import com.tweety.SwithT.common.dto.CommonErrorDto;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplySavedDto;
import com.tweety.SwithT.lecture_apply.service.LectureApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    //    과외 신청자 리스트. id는 강의 그룹
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/single-lecture-apply-list/{id}")
    public ResponseEntity<?> showSingleLectureApplyList(@PathVariable("id") Long id, @PageableDefault(size = 5)Pageable pageable){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "튜티의 과외 신청자 리스트", lectureApplyService.singleLectureApplyList(id, pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 강의 홈 - 튜티 리스트. id는 강의 그룹
    @GetMapping("/single-lecture-tutee-list/{id}")
    public ResponseEntity<?> showSingleLectureTuteeList(@PathVariable("id") Long id, @PageableDefault(size = 5)Pageable pageable){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의홈 튜티 리스트", lectureApplyService.singleLectureTuteeList(id, pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //튜터 - 매칭할 튜티에게 결제 요청 보내기. id는 lecture_apply의 id
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/single-lecture-payment-request/{id}")
    public ResponseEntity<?> singleLecturePaymentRequest(@PathVariable Long id){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "튜터가 튜티에게 결제 요청", lectureApplyService.singleLecturePaymentRequest(id));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //튜터 - 과외 신청 거절
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("/single-lecture-apply-reject/{id}")
    public ResponseEntity<?> singleLectureApplyReject(@PathVariable Long id){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "과외 수강신청 거절", lectureApplyService.singleLectureApplyReject(id));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //튜티 - 내 강의 리스트
    @PreAuthorize("hasRole('TUTEE')")
    @GetMapping("/tutee-my-lecture-list")
    public ResponseEntity<?> myLectureList(@RequestParam(value = "status")String status, @PageableDefault(size = 5)Pageable pageable){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "내 강의 리스트", lectureApplyService.myLectureList(status, pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    // 강의 신청
//    @PreAuthorize("hasRole('TUTEE')")
    @PostMapping("/lecture-apply")
    public ResponseEntity<?> tuteeLectureApply( @RequestParam Long lectureGroupId, @RequestParam Long memberId, @RequestParam String memberName) throws InterruptedException {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "튜티의 강의 신청 완료", lectureApplyService.tuteeLectureApply(lectureGroupId, memberId, memberName));
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/lecture/group/{id}")
    public ResponseEntity<?> getLectureApplyPayInfo(@PathVariable Long id){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 그룹 정보",lectureApplyService.getLectureGroupByApplyId(id));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PutMapping("lecture-apply/{id}/status")
    public ResponseEntity<?>updateLectureApplyStatus(@PathVariable("id") Long lectureApplyId,
                                                     @RequestBody CommonResDto commonResDto){
        try {
            System.out.println("feign 넘어옴");
            System.out.println("메시지: " + commonResDto.getStatus_message());
            System.out.println("결과: " + commonResDto.getResult());
            lectureApplyService.updateLectureApplyStatus(lectureApplyId, commonResDto.getResult().toString()); // 상태 업데이트 로직 구현
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), commonResDto.getStatus_message());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }
}
