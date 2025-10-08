package com.spring.schoolmate.controller;

import com.spring.schoolmate.entity.EatPhoto;
import com.spring.schoolmate.service.EatPhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spring.schoolmate.dto.eatphoto.EatPhotoRes;

import java.util.List;

@RestController
@RequestMapping("/api/v1/photos")
public class EatPhotoController {

  private final EatPhotoService eatPhotoService;

  public EatPhotoController(EatPhotoService eatPhotoService) {
    this.eatPhotoService = eatPhotoService;
  }

  @GetMapping("/students/{studentId}")
  public ResponseEntity<?> getPhotoByStudentId(@PathVariable Integer studentId) {
    List<EatPhoto> photos = eatPhotoService.getPhotoByStudentId(studentId);
    return ResponseEntity.ok(photos);
  }

  // ⭐️ 반환 타입과 메서드 호출 결과 타입을 DTO로 변경 ⭐️
  @GetMapping("/allStudentsPhotos")
  public ResponseEntity<List<EatPhotoRes>> getAllStudentPhotos() {
    List<EatPhotoRes> photos = eatPhotoService.getAllStudentPhotos();
    return ResponseEntity.ok(photos);
  }
}
