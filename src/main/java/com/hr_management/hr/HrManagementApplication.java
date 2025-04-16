package com.hr_management.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Leave management REST API", description = "Leave management REST APIs documentation", version = "v1.0", contact = @Contact(name = "KWIZERA", email = "emmizokwizera@gmail.com", url = "https://www.linkedin.com/in/kwizera-emmanuel-software-engineer/"), license = @License(name = "apache 2.0", url = "https://www.linkedin.com/in/kwizera-emmanuel-software-engineer/")), externalDocs = @ExternalDocumentation(description = "Leave management documentation", url = "https://github.com/Emmizo/leave-management"))
public class HrManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrManagementApplication.class, args);
	}

}
