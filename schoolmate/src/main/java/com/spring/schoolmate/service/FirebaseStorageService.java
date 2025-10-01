package com.spring.schoolmate.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BlobTargetOption; // 👈 컴파일 오류 해결을 위한 Import
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

  // FirebaseConfig에서 빈으로 등록된 Google Cloud Storage 객체를 주입받습니다.
  private final Storage storage;

  // application.yml에 "schoolmate-e3eef.appspot.com"이 주입된다고 가정합니다.
  @Value("${firebase.storage-bucket}")
  private String storageBucket;

  /**
   * 주입받은 storageBucket (전체 버킷 이름)을 그대로 반환합니다.
   */
  private String getFullBucketName() {
    return this.storageBucket;
  }

  /**
   * 파일을 Firebase Storage에 업로드하고 다운로드 가능한 URL을 반환합니다.
   */
  public String uploadFile(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      return null;
    }

    String fullBucketName = getFullBucketName();
    String originalFilename = file.getOriginalFilename();
    String uniqueFileName = "products/" + UUID.randomUUID().toString() + "_" + originalFilename;

    // 1. Storage 객체를 사용하여 버킷 가져오기
    Bucket bucket = storage.get(fullBucketName);

    // 버킷이 정말로 존재하지 않거나 권한 문제일 경우 명확한 오류 발생
    if (bucket == null) {
      throw new IllegalArgumentException("FATAL: Bucket " + fullBucketName + " does not exist or Service Account lacks permissions.");
    }

    // 2. BlobInfo 설정 (ACL을 PUBLIC_READ로 설정)
    BlobInfo blobInfo = BlobInfo.newBuilder(fullBucketName, uniqueFileName)
      .setContentType(file.getContentType())
      .setAcl(java.util.List.of(com.google.cloud.storage.Acl.of(
        com.google.cloud.storage.Acl.User.ofAllUsers(), com.google.cloud.storage.Acl.Role.READER)))
      .build();

    // 3. 파일 업로드 실행 (컴파일 오류 해결된 부분)
    // BlobTargetOption을 사용하여 ACL을 PUBLIC_READ로 설정하며 파일 업로드
    bucket.create(uniqueFileName,
      file.getBytes(),
      BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

    // 4. 공개 다운로드 URL 반환
    return String.format("https://storage.googleapis.com/%s/%s", fullBucketName, uniqueFileName);
  }

  /**
   * Firebase Storage에서 파일을 삭제합니다.
   */
  public void deleteFile(String imageUrl) {
    if (imageUrl == null || imageUrl.isEmpty()) return;

    try {
      String fullBucketName = getFullBucketName();

      // URL에서 버킷 이름을 제외한 파일 경로(path) 추출
      String path = imageUrl.substring(imageUrl.indexOf(fullBucketName) + fullBucketName.length() + 1);

      // 'products/'로 시작하는 경로만 추출하는 로직 (기존 로직 유지)
      int index = path.indexOf("/products/");
      if (index != -1) {
        path = path.substring(index + 1);
      }

      // Storage 객체를 사용하여 Blob을 가져와 삭제
      storage.get(fullBucketName, path).delete();
    } catch (Exception e) {
      System.err.println("Firebase 파일 삭제 실패: " + e.getMessage());
    }
  }
}