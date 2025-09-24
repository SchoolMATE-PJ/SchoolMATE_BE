package com.spring.schoolmate.controller;

import com.spring.schoolmate.service.EatPhotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/photos")
public class FileUploadController {

  private final EatPhotoService eatPhotoService;

  public FileUploadController(EatPhotoService eatPhotoService) {
    this.eatPhotoService = eatPhotoService;
  }

  @PostMapping("/upload")
  public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file, @RequestParam("studentId") Long studentId) {
    try {
      String result = eatPhotoService.uploadAndAnalyzePhoto(file.getBytes(), studentId);
      return ResponseEntity.ok(result);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}