package com.spring.schoolmate.config;

import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.repository.AdminRepository;
import com.spring.schoolmate.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

  private final AdminRepository adminRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  // application.properties에서 주입받는다.
  @Value("${app.admin.email}")
  private String adminEmail;

  @Value("${app.admin.password}")
  private String adminPassword;

  private static final String DEFAULT_ADMIN_NAME = "최초관리자";

  @Override
  public void run(String... args) throws Exception {
    // 1. ADMIN 역할이 DB에 존재하는지 확인
    if (roleRepository.findByRoleName(Role.RoleType.ADMIN).isEmpty()) {
      Role adminRole = new Role();
      adminRole.setRoleName(Role.RoleType.ADMIN);
      roleRepository.save(adminRole);
    }

    // 2. Admin 계정이 DB에 이미 존재하는지 확인
    if (!adminRepository.existsByEmail(adminEmail)) {
      // 3. Admin 계정 생성 (email: admin@school.com, password: admin)
      Admin admin = Admin.builder()
        .email(adminEmail)
        .password(passwordEncoder.encode(adminPassword))
        .name(DEFAULT_ADMIN_NAME)
        .build();
      adminRepository.save(admin);
      System.out.println("최초 Admin 계정 생성 완료: " + adminEmail);
    } else {
      System.out.println("Admin 계정 (" + adminEmail + ")이 이미 존재합니다.");
    }
  }
}