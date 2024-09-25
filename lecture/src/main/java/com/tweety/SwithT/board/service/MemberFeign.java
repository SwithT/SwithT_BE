package com.tweety.SwithT.board.service;

import com.tweety.SwithT.common.dto.SuccessResponse;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="member-service", url="http://member-service", configuration = FeignClient.class)
public interface MemberFeign {
    @GetMapping(value="member/{id}")
    SuccessResponse getMemberById(@PathVariable("id") Long id);
}
