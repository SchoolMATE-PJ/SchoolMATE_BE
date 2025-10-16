package com.spring.schoolmate.config;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI (Swagger UI) 설정을 위한 Configuration Class.
 * JWT 인증을 위한 Security Scheme을 추가합니다.
 */
@Configuration
public class SwaggerConfig {

  private static final String SCHEME_NAME = "bearerAuth";
  private static final String SCHEME = "Bearer";

  @Bean
  public OpenAPI openAPI() {
    // 1. API 문서의 메타 정보 설정 (application.yml 기반)
    Info info = new Info()
      .title("SchoolMATE API Documentation")
      .description("SchoolMATE 서비스의 백엔드 API 명세서입니다.\n" +
        "인증 (JWT), MySQL, Firebase Storage, NEIS API 연동 기능 등을 제공합니다.")
      .version("v1.0.0");

    // 2. JWT 인증을 위한 Security Scheme 정의
    SecurityScheme securityScheme = new SecurityScheme()
      .name(SCHEME_NAME)
      .type(SecurityScheme.Type.HTTP)
      .scheme(SCHEME)
      .bearerFormat("JWT"); // 토큰 포맷 지정

    // 3. Security Scheme을 Components에 등록
    Components components = new Components()
      .addSecuritySchemes(SCHEME_NAME, securityScheme);

    // 4. 모든 엔드포인트에 기본적으로 JWT 인증을 요구하도록 설정
    SecurityRequirement securityRequirement = new SecurityRequirement()
      .addList(SCHEME_NAME);

    return new OpenAPI()
      .addServersItem(new Server().url("http://localhost:9000"))
      .info(info)
      .components(components)
      .addSecurityItem(securityRequirement);
  }
}
