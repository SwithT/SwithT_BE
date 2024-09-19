package com.tweety.SwithT.service;

import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name="lecture-service", configuration = FeignConfig.class)
public interface LectureFeign {

//    @GetMapping(value = "/ads")
//    CommonResDto get~~~Id(@PathVariable("id") Long id);
//
//    @PutMapping(value = "adsad")
//
}
