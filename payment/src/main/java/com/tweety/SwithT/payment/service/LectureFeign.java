package com.tweety.SwithT.payment.service;

import com.tweety.SwithT.common.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="lecture-service", configuration = FeignConfig.class)
public interface LectureFeign {

//    @GetMapping(value = "/ads")
//    CommonResDto get~~~Id(@PathVariable("id") Long id);
//
//    @PutMapping(value = "adsad")
//
}
