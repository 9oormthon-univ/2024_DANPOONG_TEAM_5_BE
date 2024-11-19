package com.jangburich.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
@OpenAPIDefinition(
	info = @Info(title = "API Documentation", version = "1.0")
)
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info()
			.title("User Management API")
			.version("1.0")
			.description("API for managing users and their profile images.");

		return new OpenAPI()
			.info(info)
			.addSecurityItem(
				new io.swagger.v3.oas.models.security.SecurityRequirement().addList("Custom String Authentication"))
			.components(new io.swagger.v3.oas.models.Components()
				.addSecuritySchemes("Custom String Authentication",
					new io.swagger.v3.oas.models.security.SecurityScheme()
						.type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY) // API Key 타입 설정
						.name("Authorization")
						.in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER))); // 헤더로 전달
	}
}
