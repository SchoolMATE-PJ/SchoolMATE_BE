package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomAdminDetailService implements UserDetailsService {

  private final AdminRepository adminRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // Admin 테이블에서 이메일로 관리자 조회
    Admin admin = adminRepository.findByEmail(email)
      .orElseThrow(() -> new UsernameNotFoundException(
        "관리자 이메일(" + email + ")을 찾을 수 없습니다."));

    return new CustomAdminDetails(admin);
  }
}