package com.spring.schoolmate.service;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.springframework.stereotype.Service;

@Service
public class VisionService {

  private final ImageAnnotatorClient visionClient;

  // 생성자 주입: 스프링이 VisionConfig에서 만든 빈을 자동으로 주입.
  public VisionService(ImageAnnotatorClient visionClient) {
    this.visionClient = visionClient;
  }

  public String analyzeImage(byte[] imageBytes) {
    // visionClient를 사용하여 이미지 분석 로직을 수행.
    return "Analysis Result";
  }
}