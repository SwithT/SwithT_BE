package com.tweety.SwithT.common.service;

import com.tweety.SwithT.common.config.FeignConfig;
import com.tweety.SwithT.common.dto.CommonResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "http://member-service", configuration = FeignConfig.class) //배포 용도
//@FeignClient(name = "member-service", configuration = FeignConfig.class) // 로컬 용도
public interface MemberFeign {
    @GetMapping(value = "/member-name-get/{id}")
    CommonResDto getMemberNameById(@PathVariable("id")Long id);

    @GetMapping(value="/member-profile-get/{id}")
    CommonResDto getMemberProfileById(@PathVariable("id") Long id);

    @GetMapping(value="/member-score-get/{id}")
    CommonResDto getMemberScoreById(@PathVariable("id") Long id);

}