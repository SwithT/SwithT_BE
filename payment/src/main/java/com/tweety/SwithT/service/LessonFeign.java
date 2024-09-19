package com.tweety.SwithT.service;

import com.tweety.SwithT.configs.FeignConfig;
import com.tweety.SwithT.dto.LessonResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="lesson-service", configuration = FeignConfig.class)
public interface LessonFeign {

    @GetMapping("/lessons/{lessonId}")
    LessonResponseDto getLessonById(@PathVariable("lessonId") Long lessonId);
}
