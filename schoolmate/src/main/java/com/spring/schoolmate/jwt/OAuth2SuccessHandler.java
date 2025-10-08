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

    // 허용된 프론트엔드 도메인 목록 (상수로 정의)
    private static final String LOCAL_FRONTEND_URL = "http://localhost:3000";
    private static final String VERSEL_FRONTEND_URL = "https://schoolmate-fe.vercel.app";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2CustomUser oAuth2User = (OAuth2CustomUser) authentication.getPrincipal();
        Student student = oAuth2User.getStudent();

        // 1. 신규 회원 예외 처리
        if (student.getStudentId() == null) {
            log.error("CRITICAL ERROR: OAuth2SuccessHandler가 신규 회원을 받았습니다. Security 설정을 확인하세요.");

            // 신규 회원 처리 시에는 Vercel URL을 기본값으로 사용
            String signupRedirectUrl = VERSEL_FRONTEND_URL + "/login?error=signup_required";
            getRedirectStrategy().sendRedirect(request, response, signupRedirectUrl);
            return;
        }

        log.info("기존 회원 로그인을 완료하고 JWT를 발급합니다. Student ID: {}", student.getStudentId());

        // 2. 리다이렉트할 기본 URL 결정
        String baseUrl = determineTargetBaseUrl(request);

        // 3. 최종 로그인용 JWT 생성
        String token = jwtUtil.createJwt(student);

        // 4. 프론트엔드의 토큰 처리 페이지로 리다이렉트
        String redirectUrl = baseUrl + "/oauth-redirect?token=" + token;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    /**
     * 요청의 Host 또는 Origin 헤더를 기반으로 리다이렉트할 기본 URL을 결정.
     * @param request 현재 HTTP 요청
     * @return 결정된 프론트엔드 기본 URL
     */
    private String determineTargetBaseUrl(HttpServletRequest request) {
        // 실제 배포 환경에서는 'Origin' 또는 'Referer' 헤더를 확인하는 것이 일반적이지만,
        // 간단하게는 하드코딩된 도메인 중 하나를 선택할 수 있다.

        // 요청 헤더(Host 또는 Origin)에 "vercel" 문자열이 포함되어 있으면 Vercel URL을 사용
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        if (origin != null && origin.contains("vercel")) {
            return VERSEL_FRONTEND_URL;
        }

        if (referer != null && referer.contains("vercel")) {
            return VERSEL_FRONTEND_URL;
        }

        // 기본값은 Vercel URL (배포 환경)로 설정하거나,
        // Host 헤더를 분석하여 결정하는 더 정교한 로직을 사용할 수 있다.
        // 여기서는 명시적으로 localhost가 아니면 Vercel로 가정.

        if (request.getServerName().contains("localhost") || request.getServerName().equals("127.0.0.1")) {
            return LOCAL_FRONTEND_URL;
        }

        return VERSEL_FRONTEND_URL; // 기본적으로 배포 환경을 가정
    }
}