package com.spring.schoolmate.config;

import com.spring.schoolmate.jwt.JWTFilter;
import com.spring.schoolmate.jwt.JWTUtil;
import com.spring.schoolmate.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    //AuthenticationManager 가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    //AuthenticationManager Bean 등록
    /*
     * Spring Security가 주입되면 내부적으로 글로벌 영역에  AuthenticationManager 는 자동으로 주입된다
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        log.info("bCryptPasswordEncoder call..=====>");
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security의 메인 설정을 담당하는 SecurityFilterChain을 Bean으로 등록합니다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityFilterChain ===============>");
        // 1. CORS 설정 (프론트엔드 서버와의 통신을 위함)
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 2. CSRF 보호 기능 비활성화 (JWT 방식에서는 세션을 사용하지 않으므로 비활성화해도 안전합니다.)
        http.csrf(auth -> auth.disable());

        // 3. Form 기반 로그인 방식 비활성화 (우리는 JSON 기반의 커스텀 필터를 사용합니다.)
        http.formLogin(auth -> auth.disable());

        // 4. HTTP Basic 인증 방식 비활성화
        http.httpBasic(auth -> auth.disable());

        // 5. URL별 접근 권한 설정
        http.authorizeHttpRequests(auth -> auth
                // "/api/auth/**" 경로의 모든 요청은 인증 없이 허용 (회원가입, 로그인 등)
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                // 그 외의 모든 요청은 반드시 인증을 거쳐야 함
                .anyRequest().authenticated());

        // 6. 세션 관리 설정: 세션을 사용하지 않고, 모든 요청을 상태 없이(stateless) 처리하도록 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 7. 우리가 만든 JWTFilter를 UsernamePasswordAuthenticationFilter 앞에 배치합니다.
        // 모든 요청은 JWTFilter를 먼저 거쳐 토큰을 검증받습니다.
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 8. 우리가 만든 LoginFilter를 기본 UsernamePasswordAuthenticationFilter 자리에 등록(교체)합니다.
        // 이렇게 하면 JWTFilter가 자연스럽게 LoginFilter보다 앞에 위치하게 됩니다.
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }


    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 위한 Bean입니다.
     * 다른 도메인(예: http://localhost:3000)의 프론트엔드 서버가 우리 API 서버에 요청할 수 있도록 허용합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 허용할 출처
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 HTTP 메소드
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        configuration.setMaxAge(3600L);
        // 클라이언트가 응답 헤더의 "Authorization"에 접근할 수 있도록 노출
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 CORS 설정 적용
        return source;
    }
}
