// schoolmate/src/main/java/com/spring/schoolmate/config/VisionConfig.java

package com.spring.schoolmate.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class VisionConfig {

  /**
   * Vision AI 클라이언트(ImageAnnotatorClient)를 스프링 빈으로 등록.
   * FirebaseConfig에서 생성된 GoogleCredentials 빈을 주입받아 사용.
   * 이 빈은 프로파일(local/prod)에 따라 적절한 인증 정보를 사용하게 된다.
   */
  @Bean
  public ImageAnnotatorClient imageAnnotatorClient(GoogleCredentials credentials) throws IOException {

    ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .build();

    return ImageAnnotatorClient.create(settings);
  }
}