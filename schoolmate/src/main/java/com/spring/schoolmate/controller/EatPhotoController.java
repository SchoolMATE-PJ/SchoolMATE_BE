package com.spring.schoolmate.controller;

import com.spring.schoolmate.entity.EatPhoto;
import com.spring.schoolmate.service.EatPhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eatphotos")
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

  @GetMapping("/all")
  public ResponseEntity<?> getAllStudentPhotos() {
    List<EatPhoto> photos = eatPhotoService.getAllStudentPhotos();
    return ResponseEntity.ok(photos);
  }
}
