package com.spring.schoolmate.config;

import com.spring.schoolmate.jwt.JWTFilter;
import com.spring.schoolmate.jwt.JWTUtil;
import com.spring.schoolmate.jwt.LoginFilter;
import com.spring.schoolmate.jwt.OAuth2SuccessHandler;
import com.spring.schoolmate.repository.AdminRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.service.CustomOAuth2UserService;
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

        // LoginFilter 객체를 단 한 번만 생성
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        // LoginFilter 생성자에서 setFilterProcessesUrl("/api/auth/login")가 호출된다면 이 줄은 생략 가능
        // loginFilter.setFilterProcessesUrl("/api/auth/login");

        // 1. CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 2. CSRF 보호 기능 비활성화
        http.csrf(auth -> auth.disable());

        // 3. FormLogin, HttpBasic 비활성화
        http.formLogin(auth -> auth.disable());
        http.httpBasic(auth -> auth.disable());

        // 4. URL별 접근 권한 설정
        http.authorizeHttpRequests(auth -> auth
          .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/schools/**").permitAll()
          .requestMatchers("/admin").hasRole("ADMIN")
          .anyRequest().authenticated());

        // 5. 세션 관리 설정: 상태 없음(stateless)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 6. OAuth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
          .userInfoEndpoint(userInfo -> userInfo
            .userService(customOAuth2UserService)
          )
          .successHandler(oAuth2SuccessHandler)
        );

        // 7. 필터 등록 순서 정리 (중복 제거)

        // 🚨 LoginFilter를 기본 필터 자리에 등록
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // 🚨 JWTFilter를 LoginFilter보다 '앞에' 등록 (토큰 유효성 검사)
        // JWTFilter는 AdminRepository를 주입받아야 함
        http.addFilterBefore(
          new JWTFilter(jwtUtil, studentRepository, adminRepository),
          LoginFilter.class
        );

        return http.build();
    }


    /**
     * CORS 설정을 위한 Bean입니다.
     */
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