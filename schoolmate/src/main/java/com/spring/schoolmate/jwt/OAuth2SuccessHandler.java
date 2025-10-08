package com.spring.schoolmate.jwt;

import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.OAuth2CustomUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2CustomUser oAuth2User = (OAuth2CustomUser) authentication.getPrincipal();
        Student student = oAuth2User.getStudent();

        // 신규 회원은 UserNotRegisteredException으로 처리되어 이 코드가 실행되지 않는다.
        // 이 핸들러는 기존 회원 로그인 성공 시에만 호출되어야 한다.
        if (student.getStudentId() == null) {
            log.error("CRITICAL ERROR: OAuth2SuccessHandler가 신규 회원을 받았습니다. Security 설정을 확인하세요.");
            response.sendRedirect("http://localhost:3000/login?error=signup_required");
            return;
        }

        log.info("기존 회원 로그인을 완료하고 JWT를 발급합니다. Student ID: {}", student.getStudentId());

        // 1. 최종 로그인용 JWT 생성
        String token = jwtUtil.createJwt(student);

        // 2. 프론트엔드의 메인 페이지 또는 토큰 처리 페이지로 리다이렉트
        String redirectUrl = "http://localhost:3000/oauth-redirect?token=" + token;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}