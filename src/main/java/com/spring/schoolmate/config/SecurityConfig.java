package com.spring.schoolmate.config;

import com.spring.schoolmate.exception.UserNotRegisteredException;
import com.spring.schoolmate.jwt.JWTFilter;
import com.spring.schoolmate.jwt.JWTUtil;
import com.spring.schoolmate.jwt.LoginFilter;
import com.spring.schoolmate.jwt.OAuth2SuccessHandler;
import com.spring.schoolmate.repository.AdminRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.service.CustomOAuth2UserService;
import com.spring.schoolmate.security.CustomAuthorizationRequestResolver;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Arrays;
import java.util.Map;

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
    private final ClientRegistrationRepository clientRegistrationRepository;

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

    // 1. Custom Authorization Request Resolver 빈 등록 (redirect-uri 처리)
    @Bean
    public CustomAuthorizationRequestResolver authorizationRequestResolver() {
        String frontendRedirectUri = "http://localhost:3000/oauth-redirect";
        return new CustomAuthorizationRequestResolver(clientRegistrationRepository, frontendRedirectUri);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CustomAuthorizationRequestResolver authorizationRequestResolver
    ) throws Exception {
        log.info("SecurityFilterChain ===============>");

        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);

        // 1. CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 2. CSRF 보호 기능 비활성화
        http.csrf(auth -> auth.disable());

        // 3. FormLogin, HttpBasic 비활성화
        http.formLogin(auth -> auth.disable());
        http.httpBasic(auth -> auth.disable());

        // 4. URL별 접근 권한 설정
        http.authorizeHttpRequests(auth -> auth
          .requestMatchers(
            "/api/auth/**",
            "/oauth2/**",
            "/login/oauth2/code/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/school/**",
            "/api/auth/signup/social",
            "/api/school-search/**").permitAll()
          .requestMatchers("/admin").hasRole("ADMIN")
          .anyRequest().authenticated());

        // 5. 세션 관리 설정: 상태 없음(stateless)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 6. OAuth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
          .authorizationEndpoint(endpoint -> endpoint
            .authorizationRequestResolver(authorizationRequestResolver) // Custom Resolver 사용
            .baseUri("/oauth2/authorization")
          )
          .userInfoEndpoint(userInfo -> userInfo
            .userService(customOAuth2UserService)
          )
          .successHandler(oAuth2SuccessHandler)
          .failureHandler(oauth2AuthenticationFailureHandler())
          .redirectionEndpoint(endpoint -> endpoint
            .baseUri("/login/oauth2/code/*")
          )
        );

        // 7. 필터 등록 순서 정리
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://schoolmate-fe.vercel.app/"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // UserNotRegisteredException을 처리하여 /signup으로 리다이렉트하는 핸들러
    @Bean
    public AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof UserNotRegisteredException) {
                UserNotRegisteredException ex = (UserNotRegisteredException) exception;

                Map<String, Object> attributes = ex.getAttributes();
                String provider = ex.getProvider();

                String tempToken = jwtUtil.createTempSignupToken(attributes, provider);

                String email = ((Map<String, Object>) attributes.get("kakao_account")).get("email").toString();
                String nickname = ((Map<String, Object>) attributes.get("properties")).get("nickname").toString();

                String redirectUri = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect")
                  .queryParam("tempToken", tempToken)
                  .queryParam("email", email)      // 💡 추가
                  .queryParam("nickname", nickname) // 💡 추가
                  .build()
                  .encode() // 최종 빌드된 URI를 인코딩 (한글 파라미터 처리)
                  .toUriString();

                response.sendRedirect(redirectUri);
            } else {
                response.sendRedirect("/login?error");
            }
        };
    }
}