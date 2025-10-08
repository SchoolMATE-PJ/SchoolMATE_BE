package com.spring.schoolmate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
     * Production 환경에서는 Cloud Run 서비스 계정 자동 인증을 사용.
     */
    @Bean
    @Profile("prod") // prod 프로필에서만 이 빈을 생성
    public GoogleCredentials prodGoogleCredentials() throws IOException {
        // 배포 환경에서는 자동 인증 사용
        return GoogleCredentials.getApplicationDefault();
    }

    /**
     * Local 환경에서는 키 파일을 사용하여 인증합니다.
     */
    @Bean
    @Profile("local") // local 프로필에서만 이 빈을 생성
    public GoogleCredentials localGoogleCredentials() throws IOException {
        // 로컬 환경에서는 설정 파일의 경로를 사용
        Resource resource = new ClassPathResource(serviceAccountFile);
        return GoogleCredentials.fromStream(resource.getInputStream());
    }

    /**
     * 2. FirebaseApp 빈 등록 (이제 Credentials 빈을 주입받아 사용)
     * 파라미터로 받은 credentials를 사용하도록 수정
     */
    @Bean
    public FirebaseApp firebaseApp(GoogleCredentials credentials) throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setStorageBucket(storageBucket)
                    .build();
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
                .setCredentials(credentials) // credentials 빈을 직접 사용
                .build()
                .getService();
    }
}