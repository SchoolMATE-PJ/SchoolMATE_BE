package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomAdminDetails implements UserDetails {

  private final Admin admin;

  public CustomAdminDetails(Admin admin) {
    this.admin = admin;
  }

  public Admin getAdmin() {
    return admin;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Admin 엔티티의 RoleType을 기반으로 권한을 설정
    return Collections.singletonList(new SimpleGrantedAuthority(
      Role.RoleType.ADMIN.toString()));
  }

  @Override
  public String getPassword() {
    return admin.getPassword();
  }

  @Override
  public String getUsername() {
    return admin.getEmail(); // UserDetails에서는 이메일을 사용자 이름으로 사용
  }

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