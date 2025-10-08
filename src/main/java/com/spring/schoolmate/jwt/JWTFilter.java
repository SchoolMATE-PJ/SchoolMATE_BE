package com.spring.schoolmate.jwt;

import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.AdminRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.security.CustomAdminDetails;
import com.spring.schoolmate.security.CustomStudentDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    public JWTFilter(JWTUtil jwtUtil, StudentRepository studentRepository, AdminRepository adminRepository) {
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 1. 인증이 필요 없는 경로를 명시적으로 건너뛴다.
        // Spring Security Config에서 permitAll()로 설정된 경로 중
        // JWT 인증이 필요 없는 경로(소셜 회원가입, 일반 로그인/가입 등)를 여기서 제외.
        if (requestURI.startsWith("/signup/social") ||
          requestURI.startsWith("/api/auth/signup/social") ||
          requestURI.startsWith("/api/auth/login") ||
          requestURI.startsWith("/api/auth/check-") ||
          requestURI.startsWith("/api/auth/signup")) {

            // 토큰을 검사할 필요 없이 다음 필터로 즉시 진행
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        // 2. Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("token null or malformed for URI: {}", requestURI); // 로그 레벨 수정 및 URI 포함

            // 토큰이 없는 경우, 다음 필터에서 401 Unauthorized를 처리하도록 넘긴다.
            // (SecurityConfig의 anyRequest().authenticated() 설정을 따름)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 순수 토큰 획득 및 소멸 시간 검증
        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            log.warn("token expired for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 토큰에서 역할(Role), ID 획득 및 사용자 정보 조회
        String role = jwtUtil.getRole(token);
        Long id = jwtUtil.getId(token);

        UserDetails userDetails = null;

        if ("STUDENT".equals(role) || "TEMP_USER".equals(role)) {
            Optional<Student> studentOpt = studentRepository.findById(id);
            if (studentOpt.isPresent()) {
                userDetails = new CustomStudentDetails(studentOpt.get());
            }
        } else if ("ADMIN".equals(role)) {
            Optional<Admin> adminOpt = adminRepository.findById(id);
            if (adminOpt.isPresent()) {
                userDetails = new CustomAdminDetails(adminOpt.get());
            }
        }

        // 5. 사용자 정보를 찾지 못한 경우
        if (userDetails == null) {
            log.error("User not found for role {} with ID {} on URI {}", role, id, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 6. 스프링 시큐리티 인증 토큰 생성 및 등록
        Authentication authToken = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("Authentication successful for user: {} on URI {}", userDetails.getUsername(), requestURI);

        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}