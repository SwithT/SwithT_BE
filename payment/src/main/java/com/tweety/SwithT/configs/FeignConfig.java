package com.tweety.SwithT.configs;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

//    @Bean
//    public RequestInterceptor requestInterceptor(){
//        return request -> {
////            모든 feign 요청에 전역적으로 token을 세팅
//            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
//            request.header(HttpHeaders.AUTHORIZATION, token);
//        };
//    }
}
