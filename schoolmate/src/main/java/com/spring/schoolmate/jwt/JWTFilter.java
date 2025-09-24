package com.spring.schoolmate.jwt;

import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.CustomStudentDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    public JWTFilter(JWTUtil jwtUtil) {this.jwtUtil = jwtUtil;}

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String authorization= request.getHeader("Authorization");

    //Authorization 헤더 검증
    if (authorization == null || !authorization.startsWith("Bearer ")) { //인증후 들어온게 아니거나 검증된  토큰이 아니라면
      System.out.println("token null");
      filterChain.doFilter(request, response);//다음에 있는 필터로 가는 부분..갔다가 오면 아래에 있는 사후처리를 하는데..이걸 안하게 하려면 바로 return
      //조건이 해당되면 메소드 종료 (필수)
      return;
    }

      //토큰이 있다면..
      System.out.println("authorization now");
      //Bearer 부분 제거 후 순수 토큰만 획득
      String token = authorization.split(" ")[1];

      //토큰 소멸 시간 검증
      if (jwtUtil.isExpired(token)) {
          log.warn("token expired");
          //브라우져로 리플래쉬토큰을 요청
          filterChain.doFilter(request, response);
          //조건이 해당되면 메소드 종료 (필수)
          return;
      }

      //살아있는 토큰이라면 토큰에서 username과 role 획득
      Long studentId = jwtUtil.getStudentId(token);
      String email = jwtUtil.getEmail(token);
      String name = jwtUtil.getStudentName(token);
      String roleStr = jwtUtil.getRole(token);
      // String을 Role.RoleType Enum으로 변환
      Role.RoleType roleType = Role.RoleType.valueOf(roleStr);
      // Role 객체를 생성하고 Enum 값을 설정
      Role role = new Role();
      role.setRoleName(roleType);

      //userEntity를 생성하여 값 set
      Student student = Student.builder()
              .studentId(studentId)
              .email(email)
              .name(name)
              .password("temp_password")
              .role(role)
              .build();

      //UserDetails에 회원 정보 객체 담기
      CustomStudentDetails customStudentDetails = new CustomStudentDetails(student);

      //스프링 시큐리티 인증 토큰 생성
      Authentication authToken =
              new UsernamePasswordAuthenticationToken(customStudentDetails, null, customStudentDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authToken);
      filterChain.doFilter(request, response);
  }
}