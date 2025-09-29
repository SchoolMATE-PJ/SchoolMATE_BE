package com.spring.schoolmate.jwt;

import com.spring.schoolmate.entity.Admin; // 🚨 [추가] Admin 엔티티 임포트
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.AdminRepository; // 🚨 [추가] AdminRepository 임포트
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.security.CustomAdminDetails; // 🚨 [추가] CustomAdminDetails 임포트
import com.spring.schoolmate.security.CustomStudentDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 타입 임포트
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository; // 🚨 [추가] AdminRepository 필드 추가

    // 🚨 생성자 수정: StudentRepository 외에 AdminRepository도 주입받도록 함
    public JWTFilter(JWTUtil jwtUtil, StudentRepository studentRepository, AdminRepository adminRepository) {
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        // 1. Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null or malformed");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 순수 토큰 획득 및 소멸 시간 검증
        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            log.warn("token expired");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 토큰에서 역할(Role), ID 획득 및 사용자 정보 조회
        String role = jwtUtil.getRole(token);
        Long id = jwtUtil.getId(token); // 🚨 JWTUtil의 공통 getId() 메서드 사용

        UserDetails userDetails = null;

        if ("STUDENT".equals(role) || "TEMP_USER".equals(role)) { // Student 또는 임시 사용자 처리
            Optional<Student> studentOpt = studentRepository.findById(id);
            if (studentOpt.isPresent()) {
                userDetails = new CustomStudentDetails(studentOpt.get());
            }
        } else if ("ADMIN".equals(role)) { // 🚨 Admin 계정 처리
            Optional<Admin> adminOpt = adminRepository.findById(id);
            if (adminOpt.isPresent()) {
                userDetails = new CustomAdminDetails(adminOpt.get());
            }
        }

        // 4. 사용자 정보를 찾지 못한 경우 (비정상 토큰)
        if (userDetails == null) {
            log.error("User not found for role {} with ID {}", role, id);
            // 인증 실패로 처리하거나, 다음 필터로 넘겨 권한 없음을 알릴 수 있습니다.
            // 여기서는 다음 필터로 넘깁니다.
            filterChain.doFilter(request, response);
            return;
        }


        // 5. 스프링 시큐리티 인증 토큰 생성 및 등록
        Authentication authToken = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("Authentication successful for user: {}", userDetails.getUsername());

        // 6. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}