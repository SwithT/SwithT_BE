package com.tweety.SwithT.member.service;

import com.tweety.SwithT.common.configs.FeignConfig;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.member.dto.LectureStatusUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="lecture-service", url = "http://lecture-service", configuration = FeignConfig.class) //배포용도
//@FeignClient(name="lecture-service", configuration = FeignConfig.class) //로컬용도
public interface LectureFeign {

    @GetMapping(value = "/lecture-detail/{id}")
    CommonResDto getLectureById(@PathVariable("id") Long lecturePaymentId);

    @PutMapping(value = "/lectures/{id}/status")
    CommonResDto updateLectureStatus(@PathVariable("id") Long lectureId,
                                     @RequestBody LectureStatusUpdateDto lectureStatusUpdateDto);
}
