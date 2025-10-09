package com.spring.schoolmate.service;

import com.google.cloud.storage.BlobId; // ⭐️ BlobId import 추가
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BlobTargetOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException; // 인코딩 관련 import 추가
import java.net.URLDecoder; // URL 디코딩 import 추가
import java.nio.charset.StandardCharsets; // UTF-8 인코딩 import 추가
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

  // 1. 경로를 인자로 받는 메인 업로드 메서드
  /**
   * 파일을 Firebase Storage에 업로드하고 다운로드 가능한 URL을 반환.
   * @param file 업로드할 파일
   * @param folderPath 저장할 스토리지 내부 폴더 경로 (예: "profiles/" 또는 "eatphotos/")
   * @return 다운로드 URL
   */
  public String uploadFile(MultipartFile file, String folderPath) throws IOException {
    if (file.isEmpty()) {
      throw new IOException("파일이 비어 있어 Firebase 업로드가 불가능합니다.");
    }

    // 경로가 '/'로 끝나도록 보장
    String path = folderPath.endsWith("/") ? folderPath : folderPath + "/";

    String fullBucketName = getFullBucketName();
    String originalFilename = file.getOriginalFilename();

    // 최종 파일 경로는 "폴더/UUID_파일명" 형식
    String uniqueFileName = path + UUID.randomUUID().toString() + "_" + originalFilename;

    try {
      Bucket bucket = storage.get(fullBucketName);

      if (bucket == null) {
        throw new IllegalArgumentException("FATAL: Bucket " + fullBucketName + " does not exist or Service Account lacks permissions.");
      }

      // 파일 업로드 실행 및 Public Read ACL 설정
      bucket.create(uniqueFileName,
        file.getBytes(),
        BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

      // 공식 Firebase Storage 접근 URL 반환
      // uniqueFileName을 URL 인코딩해야 함
      String encodedFileName = uniqueFileName.replace("/", "%2F");
      return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
        fullBucketName, encodedFileName);

    } catch (Exception e) {
      throw new IOException("Firebase Storage 파일 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
    }
  }

  // 2. 오버로딩: 기본 폴더 "eatphotos/"를 사용하는 메서드
  /**
   * 파일을 Firebase Storage에 업로드합니다. (기본 폴더 "eatphotos/" 사용)
   */
  public String uploadFile(MultipartFile file) throws IOException {
    // 기존 eatphotos 로직을 위해 새로 만든 메서드를 기본 경로로 호출
    return uploadFile(file, "eatphotos");
  }

  // 3. 파일 삭제 메서드
  /**
   * Firebase Storage에서 파일을 삭제합니다.
   */
  public void deleteFile(String imageUrl) {
    if (imageUrl == null || imageUrl.isEmpty()) return;

    try {
      String fullBucketName = getFullBucketName();

      // 1. URL에서 파일 경로 부분 추출
      // URL 포맷: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{path}?alt=media
      int oIndex = imageUrl.indexOf("/o/");
      int altIndex = imageUrl.indexOf("?alt=media");

      if (oIndex == -1 || altIndex == -1 || oIndex >= altIndex) {
        System.err.println("Firebase 파일 삭제 실패: URL 포맷 오류");
        return;
      }

      String pathEncoded = imageUrl.substring(oIndex + 3, altIndex);

      // 2. URL 디코딩 수행 (e.g., %2F -> /)
      String filePath = URLDecoder.decode(pathEncoded, StandardCharsets.UTF_8.toString());

      // 3. Storage 객체를 사용하여 Blob을 가져와 삭제
      BlobId blobId = BlobId.of(fullBucketName, filePath);

      if (storage.delete(blobId)) {
        System.out.println("Firebase 파일 삭제 성공: " + filePath);
      } else {
        // 파일이 이미 없거나 찾을 수 없는 경우
        System.err.println("Firebase 파일 삭제 실패: 파일을 찾을 수 없거나 권한이 없습니다. 경로: " + filePath);
      }

    } catch (UnsupportedEncodingException e) {
      System.err.println("Firebase 파일 삭제 실패: URL 디코딩 오류. " + e.getMessage());
    } catch (Exception e) {
      System.err.println("Firebase 파일 삭제 중 일반 오류 발생: " + e.getMessage());
    }
  }
}