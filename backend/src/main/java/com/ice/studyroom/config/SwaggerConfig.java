package com.ice.studyroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		Info info = new Info()
			.title("HUFS ICE Study Room API")
			.version("1.0.0")
			.description("스터디룸 API 문서");

		return new OpenAPI()
			.info(info);
	}
}
