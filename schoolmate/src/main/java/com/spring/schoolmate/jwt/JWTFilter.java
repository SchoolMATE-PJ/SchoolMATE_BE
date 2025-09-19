package com.spring.schoolmate.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {
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


  }
}
