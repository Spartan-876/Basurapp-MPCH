package com.utp.Basurapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BasurappApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasurappApplication.class, args);
	}

}
