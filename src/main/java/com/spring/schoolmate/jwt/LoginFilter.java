package com.spring.schoolmate.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "로그인 및 JWT 생성")
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JWTUtil jwtUtil;
    private static final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/auth/login");
    }

    // 로그인 성공 시 실행 (Student 및 Admin 모두 처리)
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws IOException {

        log.info("로그인 성공 :: ");

        Object principal = authentication.getPrincipal();
        String token;
        Map<String, Object> responseBody = new HashMap<>();

        // 🚨 [수정] Principal 객체의 실제 타입에 따라 JWT 생성 및 응답 본문 구성
        if (principal instanceof CustomStudentDetails studentDetails) {
            // 1. Student 처리 로직
            Student student = studentDetails.getStudent();
            token = jwtUtil.createJwt(student);

            responseBody.put("id", student.getStudentId());
            responseBody.put("email", student.getEmail());
            responseBody.put("role", student.getRole().getRoleName().name());
            log.info("--- [student] ::  {} JWT 생성 완료", student.getEmail());

        } else if (principal instanceof CustomAdminDetails adminDetails) {
            // 2. Admin 처리 로직
            Admin admin = adminDetails.getAdmin();
            token = jwtUtil.createJwt(admin);

            responseBody.put("id", admin.getAdminId());
            responseBody.put("email", admin.getEmail());
            responseBody.put("role", admin.getRole().name());
            log.info("--- [admin] ::  {} JWT 생성 완료", admin.getEmail());

        } else {
            log.error("로그인 성공했으나 Principal 타입 불일치: {}", principal.getClass().getName());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(gson.toJson(Map.of("message", "서버 오류: 사용자 정보 매핑 실패")));
            return;
        }

        // 응답 설정
        response.addHeader("Authorization", "Bearer " + token);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(gson.toJson(responseBody));
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // 로그인 실패 시 실행 (유지)
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        log.info("로그인 실패: {}", failed.getMessage());
        Map<String, String> errorBody = Map.of("message", "로그인에 실패했습니다.");

        //로그인 실패시 401 응답 코드 반환
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"로그인 실패\", \"message\": \"이메일 또는 비밀번호가 일치하지 않습니다.\"}");
    }

   // JSON을 Map으로 바로 변환해서 처리하도록 함
   @Override
   public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

       String contentType = request.getContentType();

       // 1. Content-Type이 JSON인 경우, 우리가 만든 JSON 파싱 로직을 실행
       if (contentType != null && StringUtils.hasText(contentType) && contentType.contains("application/json")) {
           try {
               log.info("JSON 형식의 로그인을 시도합니다.");
               Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);
               String email = credentials.get("email");
               String password = credentials.get("password");

               UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);
               return getAuthenticationManager().authenticate(authToken);

           } catch (IOException e) {
               log.error("로그인 시도 중 JSON 파싱 에러: {}", e.getMessage());
               throw new RuntimeException("로그인 요청 처리 중 에러가 발생했습니다.", e);
           }
       }

       // 2. 그 외의 경우 (form-data 등), 부모 클래스의 원래 로직을 실행
       log.info("form-data 형식의 로그인을 시도합니다.");
       return super.attemptAuthentication(request, response);
   }
}
