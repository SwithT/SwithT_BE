package com.tweety.SwithT.payment.service;

import com.tweety.SwithT.common.configs.FeignConfig;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.payment.dto.RefundReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="lecture-service", configuration = FeignConfig.class)
public interface LectureFeign {

    @GetMapping(value = "/lecture/payment/{id}")
    CommonResDto getLectureById(@PathVariable("id") Long lecturePaymentId);

    @PostMapping(value = "/lecture/paid/status")
    CommonResDto paidStatus(CommonResDto commonResDto);

    @PostMapping(value = "/lecture/payment/refund")
    CommonResDto refundStatus(@RequestBody RefundReqDto refundReqDto);
}
