package com.spring.schoolmate.config;

import com.spring.schoolmate.jwt.JWTFilter;
import com.spring.schoolmate.jwt.JWTUtil;
import com.spring.schoolmate.jwt.LoginFilter;
import com.spring.schoolmate.jwt.OAuth2SuccessHandler;
import com.spring.schoolmate.repository.AdminRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // 추가
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.security.web.authentication.AuthenticationFailureHandler; // 추가
import org.springframework.security.core.AuthenticationException; // 추가

import java.io.IOException;
import java.util.Arrays;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityFilterChain ===============>");

        // 1. CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 2. CSRF, FormLogin, HttpBasic 비활성화
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 3. URL별 접근 권한 설정
        http.authorizeHttpRequests(auth -> auth
          .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/schools/**", "/oauth2/authorization/**").permitAll()
          .requestMatchers("/admin").hasRole("ADMIN")
          .anyRequest().authenticated());

        // 4. OAuth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
          .userInfoEndpoint(userInfo -> userInfo
            .userService(customOAuth2UserService)
          )
          .successHandler(oAuth2SuccessHandler)
        );

        // 5. 세션 관리 설정: Stateless
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 6. 필터 등록 순서 수정: LoginFilter 먼저 등록 후, JWTFilter를 그 앞에 배치

        // 6-1. LoginFilter 등록 (UsernamePasswordAuthenticationFilter 자리를 대체)
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/auth/login"); // 일반 로그인만 처리하도록 제한
        loginFilter.setAuthenticationFailureHandler(new CustomAuthenticationFailureHandler());
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class); // LoginFilter를 등록

// 6-2. JWTFilter를 LoginFilter (즉, UsernamePasswordAuthenticationFilter) 앞에 배치
        http.addFilterBefore(new JWTFilter(jwtUtil, studentRepository, adminRepository), LoginFilter.class);

        return http.build();
    }

    // 🚨 [필수] LoginFilter의 unsuccessfulAuthentication 로직을 실행하기 위한 핸들러 구현
    // LoginFilter는 UsernamePasswordAuthenticationFilter를 상속받았기 때문에
    // 기본적으로 successful/unsuccessfulAuthentication을 내부적으로 가지고 있습니다.
    // 하지만 명시적으로 FailureHandler를 설정해야 HTML 응답을 방지할 수 있습니다.
    private static class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
        private final LoginFilter loginFilter = new LoginFilter(null, null); // JWTUtil, AM은 사용하지 않으므로 null 허용

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, IOException {
            // LoginFilter의 unsuccessfulAuthentication 로직을 재사용
            loginFilter.unsuccessfulAuthentication(request, response, exception);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}