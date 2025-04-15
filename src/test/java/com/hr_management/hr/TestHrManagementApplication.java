package com.hr_management.hr;

import org.springframework.boot.SpringApplication;

public class TestHrManagementApplication {

	public static void main(String[] args) {
		SpringApplication.from(HrManagementApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
