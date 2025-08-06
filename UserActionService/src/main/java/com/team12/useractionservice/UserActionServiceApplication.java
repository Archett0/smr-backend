package com.team12.useractionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.team12.clients")
public class UserActionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserActionServiceApplication.class, args);
	}

}
