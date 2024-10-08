package com.tweety.SwithT.common.configs;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return request -> {
            // SecurityContextHolder에서 인증 객체를 가져와 JWT 토큰을 추출
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
                if (token != null && !token.isEmpty()) {
                    request.header(HttpHeaders.AUTHORIZATION, token);
                } else {
                    System.out.println("JWT Token is missing in SecurityContext.");
                }
            } else {
                System.out.println("SecurityContext contains no authentication.");
            }
        };
    }

}
