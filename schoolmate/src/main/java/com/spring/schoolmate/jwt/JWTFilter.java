package com.spring.schoolmate.jwt;

import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.repository.AdminRepository; // 🚨 [수정 1] import 추가
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.security.CustomAdminDetails;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository; // 🚨 [수정 2] AdminRepository 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

        // 🚨 [signup 문제의 해결책] 토큰이 없는 /api/auth/signup 요청은 즉시 통과
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = jwtUtil.getRole(token);
        Long userId = jwtUtil.getId(token);

        UserDetails userDetails = null;

        if (Role.RoleType.STUDENT.name().equals(role)) {
            Student student = studentRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("Student not found by token ID: " + userId));
            userDetails = new CustomStudentDetails(student);

        } else if (Role.RoleType.ADMIN.name().equals(role)) { // 🚨 [수정 3] Admin 토큰 처리 추가
            Admin admin = adminRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("Admin not found by token ID: " + userId));
            userDetails = new CustomAdminDetails(admin);

        } else {
            log.warn("토큰에 알 수 없는 역할(Role)이 포함되어 있습니다: {}", role);
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}