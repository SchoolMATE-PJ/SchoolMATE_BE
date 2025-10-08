package com.spring.schoolmate.controller;

import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.service.EatPhotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.spring.schoolmate.security.CustomStudentDetails;

@RestController
@RequestMapping("/api/v1/photos")
public class FileUploadController {

  private final EatPhotoService eatPhotoService;

  public FileUploadController(EatPhotoService eatPhotoService) {
    this.eatPhotoService = eatPhotoService;
  }

  @PostMapping("/upload")
  public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요하거나 토큰이 유효하지 않습니다.");
    }

    Long studentId;
    try {
      // JWT를 통해 인증된 학생의 ID 추출
      Object principal = authentication.getPrincipal();

      if (principal instanceof CustomStudentDetails) {
        studentId = ((CustomStudentDetails) principal).getStudent().getStudentId();
      } else if (principal instanceof Student) {
        studentId = ((Student) principal).getStudentId();
      } else {
        throw new ClassCastException("Principal 객체 타입이 CustomStudentDetails 또는 Student가 아닙니다.");
      }

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("사용자 인증 정보에서 학생 ID를 추출할 수 없습니다. (내부 로직 오류)");
    }

    try {
      // ⭐️ 수정된 부분: file.getBytes() 대신 MultipartFile 객체 'file' 자체를 전달합니다. ⭐️
      String result = eatPhotoService.uploadAndAnalyzePhoto(file, studentId);
      return ResponseEntity.ok(result);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
    } catch (Exception e) {
      // Service에서 throw new DMLException(...) 등으로 던진 메시지를 반환합니다.
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}