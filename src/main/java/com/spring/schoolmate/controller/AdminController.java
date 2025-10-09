package com.spring.schoolmate.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.spring.schoolmate.dto.admin.AdminProfileUpdateReq;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.service.AdminProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

  private final AdminProfileService adminProfileService;

  /**
   * 1. 특정 학생의 정보 및 포인트 수정 API
   * PUT /api/admin/students/{studentId}/profile
   */
  @PutMapping("/students/{studentId}/profile")
  public ResponseEntity<ProfileRes> updateStudentProfile(
    @PathVariable Long studentId,
    @RequestBody AdminProfileUpdateReq req) {

    ProfileRes updatedProfile = adminProfileService.updateStudentProfile(studentId, req);
    return ResponseEntity.ok(updatedProfile);
  }

  /**
   * 2. 모든 학생의 목록과 프로필, 현재 보유 포인트를 조회하는 API
   * GET /api/admin/students
   * Pageable을 인자로 받아 페이징 및 검색 조건을 처리하도록 수정
   */
  @GetMapping("/students")
  public ResponseEntity<Page<ProfileRes>> getStudents(
    // Pageable 객체를 사용하여 page, size, sort 파라미터를 자동 바인딩
    Pageable pageable,
    // 검색 조건 추가 (프론트엔드의 searchBy.key에 대응)
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String phone,
    @RequestParam(required = false) String school
  ) {
    Page<ProfileRes> studentPage = adminProfileService.getStudentProfiles(
      pageable, name, phone, school
    );
    return ResponseEntity.ok(studentPage);
  }
}