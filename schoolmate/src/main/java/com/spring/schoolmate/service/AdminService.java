package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.admin.AdminReq;
import com.spring.schoolmate.dto.admin.AdminRes;
import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 새로운 관리자(Admin) 계정 등록.
   * @param request 관리자 정보 DTO
   * @return 저장된 Admin 응답 DTO
   * @throws IllegalArgumentException 이메일이 이미 사용 중일 경우
   */
  @Transactional // 쓰기 작업
  public AdminRes createAdmin(AdminReq request) {
    // 이메일 중복 검사
    if (adminRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 사용중인 관리자 이메일입니다.");
    }

    // Admin 엔티티 생성 및 비밀번호 암호화
    Admin newAdmin = Admin.builder()
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .name(request.getName())
      .build();

    Admin savedAdmin = adminRepository.save(newAdmin);

    // 응답 DTO 변환
    return AdminRes.builder()
      .adminId(savedAdmin.getAdminId())
      .email(savedAdmin.getEmail())
      .name(savedAdmin.getName())
      // Admin 엔티티에 기본으로 RoleType.ADMIN이 설정되어 있음
      .role(savedAdmin.getRole().toString())
      .build();
  }

  /**
   * 이메일로 Admin 엔티티를 조회. (인증/인가에 사용)
   * @param email 조회할 이메일
   * @return Admin 엔티티
   * @throws NoSuchElementException 이메일에 해당하는 관리자를 찾을 수 없을 경우
   */
  public Admin findAdminByEmail(String email) {
    return adminRepository.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 관리자를 찾을 수 없습니다."));
  }
}