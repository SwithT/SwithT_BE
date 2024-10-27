package com.tweety.SwithT.payment.service;

import com.tweety.SwithT.common.configs.FeignConfig;
import com.tweety.SwithT.common.dto.CommonResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="lecture-service", configuration = FeignConfig.class)
public interface LectureFeign {

//    @GetMapping(value = "/lecture/group/{id}")
//    CommonResDto getLectureApplyPayInfo(@PathVariable("id") Long lecturePaymentId);

    @PutMapping(value = "/lecture-apply/{id}/status")
    CommonResDto updateLectureApplyStatus(@PathVariable("id") Long lectureApplyId, @RequestBody CommonResDto commonResDto);

    @PutMapping(value = "/lectures/{id}/payment/refund")
    void requestRefund(@PathVariable("id") Long lectureGroupId);

    @GetMapping(value = "/lecture-group/get/remaining/{id}")
    int getRemaining(@PathVariable("id")Long lectureGroupId);

    @PostMapping(value = "/lecture/after-paid")
    ResponseEntity<?> updateLectureStatus(@RequestParam("lectureGroupId") Long lectureGroupId,
                                          @RequestParam("memberId") Long memberId);
}
