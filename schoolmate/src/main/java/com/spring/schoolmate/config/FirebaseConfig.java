package com.spring.schoolmate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

  @Value("${firebase.service-account-file}")
  private String serviceAccountFile;

  @Value("${firebase.storage-bucket}")
  private String storageBucket;

  /**
   * 1. GoogleCredentials 객체를 별도의 빈으로 분리하여 재사용성을 높입니다.
   */
  @Bean
  public GoogleCredentials googleCredentials() throws IOException {
    Resource resource = new ClassPathResource(serviceAccountFile);
    return GoogleCredentials.fromStream(resource.getInputStream());
  }

  /**
   * 2. Firebase Admin SDK 초기화 및 FirebaseApp 빈 등록
   * 이제 @Bean 메서드가 FirebaseApp을 생성하고, Spring 컨테이너가 이 빈의 생성을 관리합니다.
   * Credentials 빈을 주입받아 사용합니다.
   */
  @Bean
  public FirebaseApp firebaseApp(GoogleCredentials credentials) throws IOException {
    // 2. FirebaseOptions 구성
    FirebaseOptions options = FirebaseOptions.builder()
      .setCredentials(credentials) // 👈 주입받은 credentials 사용
      .setStorageBucket(storageBucket)
      .build();

    // 3. Firebase 초기화
    if (FirebaseApp.getApps().isEmpty()) {
      return FirebaseApp.initializeApp(options);
    } else {
      return FirebaseApp.getInstance();
    }
  }

  /**
   * 3. Storage 빈 등록 (Credentials 오류 해결)
   * FirebaseApp 대신, GoogleCredentials 빈을 직접 주입받아 사용합니다.
   */
  @Bean
  public Storage storage(FirebaseApp firebaseApp, GoogleCredentials credentials) {
    // FirebaseApp에서 직접 credentials를 꺼내지 않고, 이미 생성된 credentials 빈을 사용합니다.
    return StorageOptions.newBuilder()
      .setProjectId(firebaseApp.getOptions().getProjectId())
      .setCredentials(credentials) // 👈 credentials 빈을 직접 사용
      .build()
      .getService();
  }
}