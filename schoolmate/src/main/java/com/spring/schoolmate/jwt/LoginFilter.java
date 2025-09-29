package com.spring.schoolmate.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.CustomStudentDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j(topic = "로그인 및 JWT 생성")
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JWTUtil jwtUtil;
    private static final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        super(authenticationManager); // 부모 클래스에 AuthenticationManager 전달
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/auth/login");
    }

    // 로그인 성공 시 실행
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws IOException {
        log.info("로그인 성공 :: ");
        CustomStudentDetails userDetails = (CustomStudentDetails) authentication.getPrincipal();
        Student student = userDetails.getStudent();
        String token = jwtUtil.createJwt(student);
        System.out.println("--- [student] ::  "+ student +" @@@@@@@@@@@@@@@@@@");

        //응답할 헤더를 설정
        //베어러 뒤에 공백을 준다. 관례적인  prefix
        response.addHeader("Authorization", "Bearer " + token);

        // 2. 응답 본문에 담을 사용자 정보 Map 생성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("studentId", student.getStudentId());
        responseBody.put("email", student.getEmail());
        responseBody.put("name", student.getName());

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(gson.toJson(responseBody));
        response.setStatus(HttpServletResponse.SC_OK);
    }

    //CustomMemberDetailsService에서 null이 떨어지면 이곳으로 리턴..
    //응답 메세지를 Json형태로 프론크 단으로 넘기기 위해서 Gson 라이브러리 사용함.
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
