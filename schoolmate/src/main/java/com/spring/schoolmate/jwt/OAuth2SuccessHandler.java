package com.spring.schoolmate.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.OAuth2CustomUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

        // Student 객체에 studentId가 있는지 여부로 신규/기존 회원 판별
        boolean isNewUser = (student.getStudentId() == null);

        if (isNewUser) {
            log.info("신규 소셜 회원을 추가 정보 입력 페이지로 리다이렉트합니다.");
            try {
                // 1. 회원가입을 마저 진행하기 위한 임시 토큰 생성
                String tempToken = jwtUtil.createTempSignupToken(oAuth2User);
                log.error("!!!!!!!!!! 임시 토큰 생성 성공! TOKEN: {} !!!!!!!!!", tempToken);

                // 2. 프론트에서 닉네임을 바로 쓸 수 있도록 카카오 닉네임을 추출
                Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                String nickname = profile.get("nickname").toString();

                // 3. 프론트엔드의 추가 정보 입력 페이지로 리다이렉트 (임시 토큰, 닉네임 전달)
                String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/signup")
                        .queryParam("tempToken", tempToken)
                        .queryParam("nickname", nickname)
                        .build()
                        .encode(StandardCharsets.UTF_8)
                        .toUriString();

                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            } catch (JsonProcessingException e) {
                log.error("임시 토큰 생성 중 오류가 발생했습니다.", e);
                // TODO: 에러 페이지로 리다이렉트 하는 로직 추가
            }
        } else {
            log.info("기존 회원 로그인을 완료하고 JWT를 발급합니다. Student ID: {}", student.getStudentId());
            // 1. 최종 로그인용 JWT 생성
            String token = jwtUtil.createJwt(student);
            // 2. 프론트엔드의 메인 페이지 또는 토큰 처리 페이지로 리다이렉트
            String redirectUrl = "http://localhost:3000/oauth-redirect?token=" + token;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }
}
