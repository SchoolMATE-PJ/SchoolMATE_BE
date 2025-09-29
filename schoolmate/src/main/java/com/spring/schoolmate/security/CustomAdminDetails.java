package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Admin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Getter
public class CustomAdminDetails implements UserDetails {

  private final Admin admin;

  public CustomAdminDetails(Admin admin) {
    this.admin = admin;
    log.info("CustomAdminDetails===>{}", admin.getEmail());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Admin 권한 반환
    log.info("getAuthorities() [Admin] ==========>");
    String authority = admin.getRole().toString();
    // Spring Security의 권한은 "ROLE_" 접두사가 붙는 것이 관례입니다.
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + authority));
  }

  @Override
  public String getPassword() {
    log.info("getPassword() [Admin] ===>");
    return admin.getPassword();
  }

  @Override
  public String getUsername() {
    log.info("getUsername() [Admin] ===>");
    // Admin 계정의 식별자로 Email을 사용
    return admin.getEmail();
  }

  // Admin은 만료되지 않는다고 가정하고 모두 true 반환 (필요에 따라 로직 수정 가능)
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}