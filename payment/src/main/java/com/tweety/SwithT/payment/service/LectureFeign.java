package com.tweety.SwithT.payment.service;

import com.tweety.SwithT.common.configs.FeignConfig;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.payment.dto.RefundReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="lecture-service", configuration = FeignConfig.class)
public interface LectureFeign {

//    @GetMapping(value = "/lecture/group/{id}")
//    CommonResDto getLectureApplyPayInfo(@PathVariable("id") Long lecturePaymentId);

    @PutMapping(value = "/lecture-apply/{id}/status")
    CommonResDto updateLectureApplyStatus(@PathVariable("id") Long lectureApplyId, @RequestBody CommonResDto commonResDto);

    @PostMapping(value = "/lectures/{id}/payment/refund")
    CommonResDto requestRefund(@PathVariable("id") Long lectureApplyId, @RequestBody RefundReqDto refundReqDto);
}
