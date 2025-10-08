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
import jakarta.servlet.http.HttpServletRequest;
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

    private static final String LOCAL_FRONTEND_URL = "http://localhost:3000";
    private static final String VERSEL_FRONTEND_URL = "https://schoolmate-fe.vercel.app/";

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
        // 이 URI는 백엔드 서버의 URL을 따라가므로 그대로 둔다.
        // Spring Security가 런타임에 base URL을 결정.
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
            "/api/school-search/**",
            "/api/students/**",
            "/api/profile/**"
            ).permitAll()
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
          // CustomFailureHandler는 요청을 받으므로 RequestResolver를 넘긴다.
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
     * CORS 설정.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 수정: 로컬 주소 제거 및 Vercel 주소의 불필요한 슬래시 제거, 와일드카드 사용 권장
        configuration.setAllowedOriginPatterns(Arrays.asList(
          "http://localhost:3000",
          "http://localhost:9000",
          "https://schoolmate-fe.vercel.app",
          "https://*.vercel.app",  // Vercel 프리뷰 배포도 허용
          "https://schoolmate-*.run.app"  // Cloud Run URL도 허용
        ));

        // 또는 모든 도메인 허용 (Cloud Run에서 자주 사용됨):
        // configuration.setAllowedOriginPatterns(Arrays.asList("*"));

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

                // 1. 리다이렉트할 베이스 URL 동적 결정
                String frontendBaseUrl = getFrontendBaseUrl(request);

                Map<String, Object> attributes = ex.getAttributes();
                String provider = ex.getProvider();

                String tempToken = jwtUtil.createTempSignupToken(attributes, provider);

                // 2. 카카오 계정 정보 추출
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

                String email = kakaoAccount != null && kakaoAccount.containsKey("email") ? kakaoAccount.get("email").toString() : null;
                String nickname = properties != null && properties.containsKey("nickname") ? properties.get("nickname").toString() : null;

                // 3. 최종 리다이렉트 URI 생성 (동적 URL 사용)
                String redirectUri = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/oauth-redirect") // 베이스 URL 동적 결정
                  .queryParam("tempToken", tempToken)
                  .queryParam("email", email)
                  .queryParam("nickname", nickname)
                  .build()
                  .encode()
                  .toUriString();

                response.sendRedirect(redirectUri);
            } else {
                response.sendRedirect("/login?error");
            }
        };
    }

    /**
     * 요청의 Host 또는 Origin 헤더를 기반으로 리다이렉트할 기본 URL을 결정.
     * OAuth2SuccessHandler에 있는 로직을 재사용.
     */
    private String getFrontendBaseUrl(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        // Vercel에서 요청이 왔는지 확인
        if (origin != null && origin.contains("vercel")) {
            return VERSEL_FRONTEND_URL;
        }

        if (referer != null && referer.contains("vercel")) {
            return VERSEL_FRONTEND_URL;
        }

        // 로컬 환경인지 확인
        if (request.getServerName().contains("localhost") || request.getServerName().equals("127.0.0.1")) {
            return LOCAL_FRONTEND_URL;
        }

        return VERSEL_FRONTEND_URL; // 기본적으로 배포 환경을 가정
    }
}