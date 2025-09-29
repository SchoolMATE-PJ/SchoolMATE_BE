package com.spring.schoolmate.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.CustomAdminDetails;
import com.spring.schoolmate.security.CustomStudentDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JWTUtil jwtUtil;
    // 🚨 [수정 1] AuthenticationManager 필드 제거. super()를 통해 주입받고, getAuthenticationManager()를 사용합니다.

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🚨 [수정 2] 부모 생성자를 명시적으로 호출합니다.
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        super(authenticationManager); // <<-- AuthenticationManager를 부모에게 전달
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            Map<String, String> loginData = objectMapper.readValue(
              StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8),
              Map.class
            );

            String email = loginData.get("email"); // Postman 요청 Body는 반드시 "email" 키를 사용해야 합니다.
            String password = loginData.get("password");

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

            // 🚨 [수정 3] 부모 클래스의 메서드를 사용하여 manager를 가져와 인증 시도
            return super.getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            log.error("Failed to read login request body", e);
            throw new RuntimeException(e);
        }
    }

    // ... (successfulAuthentication, unsuccessfulAuthentication 유지)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        // ... (로그인 성공 로직 유지)
        Object principal = authResult.getPrincipal();
        String token;
        Map<String, Object> responseBody = new HashMap<>();

        if (principal instanceof CustomStudentDetails studentDetails) {
            Student student = studentDetails.getStudent();
            token = jwtUtil.createJwt(student);

            responseBody.put("id", student.getStudentId());
            responseBody.put("email", student.getEmail());
            responseBody.put("role", student.getRole().getRoleName().name());

        } else if (principal instanceof CustomAdminDetails adminDetails) {
            Admin admin = adminDetails.getAdmin();
            token = jwtUtil.createJwt(admin);

            responseBody.put("id", admin.getAdminId());
            responseBody.put("email", admin.getEmail());
            responseBody.put("role", admin.getRole().name());

        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("인증된 Principal 타입이 예상과 다릅니다.");
            return;
        }

        response.addHeader("Authorization", "Bearer " + token);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        response.getWriter().print(new ObjectMapper().writeValueAsString(responseBody));
    }

    @Override
    public void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        // ... (로그인 실패 로직 유지)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("message", "로그인 실패: 자격 증명이 유효하지 않습니다.");

        response.getWriter().print(new ObjectMapper().writeValueAsString(errorBody));
    }
}