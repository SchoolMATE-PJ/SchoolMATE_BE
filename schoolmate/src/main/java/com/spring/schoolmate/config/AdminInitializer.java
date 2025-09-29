package com.spring.schoolmate.config;

import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

  private final AdminRepository adminRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Value("${app.admin.email}")
  private String adminEmail;

  @Value("${app.admin.password}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    Optional<Admin> existingAdminOpt = adminRepository.findByEmail(adminEmail);

    if (existingAdminOpt.isEmpty()) {
      // 1. 계정이 존재하지 않으면, 새로운 Admin 계정 생성 (최초 실행)
      String encodedPassword = bCryptPasswordEncoder.encode(adminPassword);

      Admin admin = Admin.builder()
        .email(adminEmail)
        .password(encodedPassword)
        .role(Role.RoleType.ADMIN)
        .build();

      adminRepository.save(admin);
      log.info("⭐ Initial Admin account created: {}", adminEmail);

    } else {
      // 2. 계정이 존재함: 비밀번호 유효성 검사 및 필요시 업데이트
      Admin existingAdmin = existingAdminOpt.get();

      // BCryptPasswordEncoder.matches()를 사용하여 평문과 DB 해시 값을 비교
      if (!bCryptPasswordEncoder.matches(adminPassword, existingAdmin.getPassword())) {

        // 비밀번호가 일치하지 않으면 (DB에서 변경되었거나 손상된 경우), YAML 값으로 재설정
        String newEncodedPassword = bCryptPasswordEncoder.encode(adminPassword);

        existingAdmin.setPassword(newEncodedPassword);
        adminRepository.save(existingAdmin);

        log.warn("⚠️ Admin account password RESET to YAML value for {}. Login should now work with the configured password.", adminEmail);

      } else {
        log.info("Admin account already exists and password matches: {}", adminEmail);
      }
    }
  }
}