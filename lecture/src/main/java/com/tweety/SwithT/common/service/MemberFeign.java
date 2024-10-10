package com.tweety.SwithT.common.service;

import com.tweety.SwithT.common.config.FeignConfig;
import com.tweety.SwithT.common.dto.CommonResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", configuration = FeignConfig.class)
public interface MemberFeign {
    @GetMapping(value = "/member-name-get/{id}")
    CommonResDto getMemberNameById(@PathVariable("id")Long id);

    @GetMapping(value="/member-profile-get/{id}")
    CommonResDto getMemberProfileById(@PathVariable("id") Long id);
}