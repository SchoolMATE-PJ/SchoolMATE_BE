package com.spring.schoolmate.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.spring.schoolmate.dto.eatphoto.EatPhotoRes;
import com.spring.schoolmate.entity.EatPhoto;
import com.spring.schoolmate.entity.EatphotoVisionScore;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.VisionLabel;
import com.spring.schoolmate.exception.DMLException;
import com.spring.schoolmate.repository.EatPhotoRepository;
import com.spring.schoolmate.repository.EatphotoVisionScoreRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.repository.VisionLabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EatPhotoService {

  private final EatPhotoRepository eatPhotoRepository;
  private final EatphotoVisionScoreRepository eatphotoVisionScoreRepository;
  private final VisionLabelRepository visionLabelRepository;
  private final StudentRepository studentRepository;
  private final TranslationService translationService;
  private final FirebaseStorageService firebaseStorageService;
  private final PointHistoryService pointHistoryService; // 포인트 지급 서비스
  private final ImageAnnotatorClient visionClient;

  public EatPhotoService(
    EatPhotoRepository eatPhotoRepository,
    EatphotoVisionScoreRepository eatphotoVisionScoreRepository,
    VisionLabelRepository visionLabelRepository,
    StudentRepository studentRepository,
    TranslationService translationService,
    FirebaseStorageService firebaseStorageService,
    PointHistoryService pointHistoryService,
    ImageAnnotatorClient visionClient) { // 생성자 매개변수와 본문 수정
    this.eatPhotoRepository = eatPhotoRepository;
    this.eatphotoVisionScoreRepository = eatphotoVisionScoreRepository;
    this.visionLabelRepository = visionLabelRepository;
    this.studentRepository = studentRepository;
    this.translationService = translationService;
    this.firebaseStorageService = firebaseStorageService;
    this.pointHistoryService = pointHistoryService;
    this.visionClient = visionClient; // 클라이언트 저장
  }

  // 업로드된 사진을 분석하고 결과를 저장
  public String uploadAndAnalyzePhoto(MultipartFile file, Long studentId) throws Exception {

    // 1. 학생 ID로 학생 엔티티를 찾습니다.
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new DMLException("학생의 고유 아이디를 찾지 못했습니다.: " + studentId));

    String imageUrl = null;
    try {
      // 2. Firebase Storage에 파일 업로드 및 URL 획득 (NULL 오류 해결)
      imageUrl = firebaseStorageService.uploadFile(file);
    } catch (IOException e) {
      throw new DMLException("이미지 업로드에 실패했습니다. (Storage Error): " + e.getMessage());
    }

    // 3. 새로운 급식 사진(EatPhoto) 엔티티를 생성하고 데이터베이스에 저장
    EatPhoto eatPhoto = new EatPhoto();
    eatPhoto.setStudent(student);
    eatPhoto.setEatimageUrl(imageUrl);
    eatPhoto.setEatuploadedAt(LocalDateTime.now());
    final EatPhoto savedEatPhoto = eatPhotoRepository.save(eatPhoto);

    // 4. Google Cloud Vision AI API를 호출하여 이미지 분석을 수행
    byte[] photoData = file.getBytes();

    try {
      ByteString imgBytes = ByteString.copyFrom(photoData);
      Image image = Image.newBuilder().setContent(imgBytes).build();
      Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feature)
        .setImage(image)
        .build();

      BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(Collections.singletonList(request));
      AnnotateImageResponse annotateImageResponse = response.getResponses(0);


      // 5. 분석 결과를 처리하고 데이터베이스에 저장
      boolean isSchoolLunch = false;
      int recognizedFoodCount = 0;

      for (EntityAnnotation label : annotateImageResponse.getLabelAnnotationsList()) {
        String englishLabelName = label.getDescription();
        Float score = label.getScore();

        if ((englishLabelName.equalsIgnoreCase("Food") || englishLabelName.equalsIgnoreCase("Meal") || englishLabelName.equalsIgnoreCase("Cuisine")) && score > 0.80f) {
          isSchoolLunch = true;
          recognizedFoodCount++;
        }

        String koreanLabelName = translationService.translate(englishLabelName, "ko");

        Optional<VisionLabel> existingLabel = visionLabelRepository.findByLabelName(koreanLabelName);
        VisionLabel visionLabel = existingLabel.orElseGet(() -> {
          return visionLabelRepository.save(
            VisionLabel.builder()
              .labelName(koreanLabelName)
              .languageCode("ko")
              .build()
          );
        });

        EatphotoVisionScore visionScore = EatphotoVisionScore.builder()
          .eatphoto(savedEatPhoto)
          .visionLabel(visionLabel)
          .score(score)
          .build();
        eatphotoVisionScoreRepository.save(visionScore);
      }

      // 6. 최종 급식 사진 여부에 따라 포인트 지급 및 결과 반환
      if (isSchoolLunch && recognizedFoodCount > 0) {
        final int POINT_AMOUNT = 2000;

        // PointHistoryService.addPointTransaction 호출
        pointHistoryService.addPointTransaction(studentId, POINT_AMOUNT, "급식 사진 업로드");
        return "급식 사진이 확인되어 " + POINT_AMOUNT + "포인트가 지급되었습니다.";
      } else {
        return "급식 사진이 아닙니다. 다시 시도해 주세요.";
      }
    } catch (Exception e) {
      // Vision AI 관련 오류 발생 시 처리
      throw new Exception("Vision AI 분석 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  // 특정 학생이 업로드한 모든 사진을 조회
  public List<EatPhoto> getPhotoByStudentId(Integer studentId) {
    return eatPhotoRepository.findByStudent_StudentId(studentId);
  }

  // 반환 타입을 EatPhotoRes 리스트로 변경
  @Transactional(readOnly = true)
  public List<EatPhotoRes> getAllStudentPhotos() {
    // JPQL 쿼리는 FETCH JOIN으로 Profile까지 모두 가져왔기 때문에 여기서 안전하게 접근 가능
    List<EatPhoto> photos = eatPhotoRepository.findAllStudentPhotos();

    // 엔티티를 DTO로 변환
    return photos.stream()
      .map(EatPhotoRes::fromEntity)
      .collect(Collectors.toList());
  }
}