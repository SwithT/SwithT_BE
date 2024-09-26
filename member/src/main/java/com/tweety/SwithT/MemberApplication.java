package com.tweety.SwithT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class  MemberApplication {

	public static void main(String[] args) {

		SpringApplication.run(MemberApplication.class, args);

	}
}
