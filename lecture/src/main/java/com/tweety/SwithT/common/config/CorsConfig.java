package com.tweety.SwithT.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("https://www.switht.co.kr", "https://server.switht.co.kr","http://localhost:8081", "https://www.woojin.today", "https://server.woojin.today")
                .allowedMethods("*")    //get, post, ...
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
