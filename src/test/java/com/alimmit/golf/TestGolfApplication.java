package com.alimmit.golf;

import org.springframework.boot.SpringApplication;

public class TestGolfApplication {

	public static void main(String[] args) {
		SpringApplication.from(GolfApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
