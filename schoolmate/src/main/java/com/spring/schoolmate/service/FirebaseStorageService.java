package com.spring.schoolmate.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BlobTargetOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

  private final Storage storage;

  @Value("${firebase.storage-bucket}")
  private String storageBucket;

  private String getFullBucketName() {
    return this.storageBucket;
  }

  /**
   * 파일을 Firebase Storage에 업로드하고 다운로드 가능한 URL을 반환합니다.
   */
  public String uploadFile(MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new IOException("파일이 비어 있어 Firebase 업로드가 불가능합니다.");
    }

    String fullBucketName = getFullBucketName();
    String originalFilename = file.getOriginalFilename();
    String uniqueFileName = "eatphotos/" + UUID.randomUUID().toString() + "_" + originalFilename;

    try {
      Bucket bucket = storage.get(fullBucketName);

      if (bucket == null) {
        throw new IllegalArgumentException("FATAL: Bucket " + fullBucketName + " does not exist or Service Account lacks permissions.");
      }

      BlobInfo blobInfo = BlobInfo.newBuilder(fullBucketName, uniqueFileName)
        .setContentType(file.getContentType())
        .build();

      // 파일 업로드 실행 및 Public Read ACL 설정
      bucket.create(uniqueFileName,
        file.getBytes(),
        BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

      // 공식 Firebase Storage 접근 URL 반환
      return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
        fullBucketName, uniqueFileName.replace("/", "%2F"));

    } catch (Exception e) {
      // 업로드 실패 시 정확한 예외를 던짐
      throw new IOException("Firebase Storage 파일 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
    }
  }

  /**
   * Firebase Storage에서 파일을 삭제합니다.
   */
  public void deleteFile(String imageUrl) {
    if (imageUrl == null || imageUrl.isEmpty()) return;

    try {
      String fullBucketName = getFullBucketName();

      // URL에서 버킷 이름을 제외한 파일 경로(path) 추출 (Firebase URL 포맷 기준)
      int oIndex = imageUrl.indexOf("/o/");
      int altIndex = imageUrl.indexOf("?alt=media");

      if (oIndex == -1 || altIndex == -1 || oIndex >= altIndex) {
        System.err.println("Firebase 파일 삭제 실패: URL 포맷 오류");
        return;
      }

      String pathEncoded = imageUrl.substring(oIndex + 3, altIndex);
      String path = pathEncoded.replace("%2F", "/"); // 디코딩

      // Storage 객체를 사용하여 Blob을 가져와 삭제
      if (storage.get(fullBucketName, path) != null) {
        storage.get(fullBucketName, path).delete();
      }

    } catch (Exception e) {
      System.err.println("Firebase 파일 삭제 실패: " + e.getMessage());
    }
  }
}