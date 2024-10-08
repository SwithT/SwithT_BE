//package com.tweety.SwithT.common.configs;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//@RequiredArgsConstructor
//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("http://localhost:8080")
//                        .allowedOrigins("http://localhost:8081")
////                        .allowedOrigins("/member-service/login/oauth2/code/google") 테스트 후 삭제
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                        .allowedHeaders("*");
//            }
//        };
//    }
//
//}
