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

@EnableWebSecurity
@Configuration
@Slf4j
@RequiredArgsConstructor
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

//   @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception {
//        log.info("SecurityFilterChain ===============>");
//        http.csrf(auth -> auth.disable());
//        http.formLogin(auth -> auth.disable());
//        http.httpBasic(auth -> auth.disable());
//
//        http.authorizeHttpRequests(auth -> auth
//                // .requestMatchers("/login").permitAll() // 로그인 경로 허용
//                .requestMatchers( "/students", "/students/**", "/api/boards/").permitAll()
//                .requestMatchers("/admin").hasRole("ADMIN")
//                .requestMatchers("/swagger-ui/**").permitAll()
//                .requestMatchers("/api-docs/**").permitAll()
//
//                .anyRequest().authenticated());
//
//
//        //추가!!! 중요!!!
//        //JWT 사용하는 순간...Session방식 사용안하게 된다.
//        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//        //JWTFilter를 LoginFilter앞에 추가!! jwt토큰정보를 얘가 먼저 가로챈다
//        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
//
//        //UsernamePasswordAuthenticationFilter 자리에 LoginFilter가 들어간다
//        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil),
//                UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000"); // 리액트 앱의 출처
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        //이 부분을 추가하면 브라우저 콘솔창에 토큰 정보를 직접 확인할 있다
        config.addExposedHeader("Authorization");

        source.registerCorsConfiguration("/**", config);
        return source;

    }
}
