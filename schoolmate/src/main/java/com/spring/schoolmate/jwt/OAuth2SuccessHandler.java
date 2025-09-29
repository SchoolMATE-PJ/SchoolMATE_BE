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

            // 1. 회원가입을 마저 진행하기 위한 임시 토큰 생성
            // 🚨 JWTUtil에 createTempSignupToken(OAuth2CustomUser) 구현이 전제됩니다.
            String tempToken = jwtUtil.createTempSignupToken(oAuth2User);

            // 2. 프론트에서 닉네임을 바로 쓸 수 있도록 카카오 닉네임을 추출 (기존 로직 유지)
            // Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            // Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            // String nickname = profile.get("nickname").toString();

            // 닉네임 추출 로직을 간결하고 안전하게 처리 (null 체크 및 타입 캐스팅 오류 방지)
            String nickname = getNicknameFromOAuth2Attributes(oAuth2User.getAttributes());


            // 3. 프론트엔드의 추가 정보 입력 페이지로 리다이렉트 (임시 토큰, 닉네임 전달)
            String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/social-signup")
              .queryParam("tempToken", tempToken)
              .queryParam("nickname", nickname)
              .build()
              .encode(StandardCharsets.UTF_8)
              .toUriString();

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } else {
            log.info("기존 회원 로그인을 완료하고 JWT를 발급합니다. Student ID: {}", student.getStudentId());

            // 1. 최종 로그인용 JWT 생성
            String token = jwtUtil.createJwt(student);

            // 2. 프론트엔드의 메인 페이지 또는 토큰 처리 페이지로 리다이렉트
            // 토큰을 URL 쿼리 파라미터로 전달
            String redirectUrl = "http://localhost:3000/oauth-redirect?token=" + token;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

    /**
     * OAuth2 속성 Map에서 닉네임을 안전하게 추출하는 헬퍼 메서드 (카카오에 최적화)
     */
    private String getNicknameFromOAuth2Attributes(Map<String, Object> attributes) {
        try {
            if (attributes.containsKey("kakao_account")) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                if (kakaoAccount.containsKey("profile")) {
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    if (profile.containsKey("nickname")) {
                        return profile.get("nickname").toString();
                    }
                }
            }
        } catch (Exception e) {
            log.error("OAuth2 속성에서 닉네임 추출 중 오류 발생", e);
        }
        return "New User"; // 추출 실패 시 기본값
    }
}