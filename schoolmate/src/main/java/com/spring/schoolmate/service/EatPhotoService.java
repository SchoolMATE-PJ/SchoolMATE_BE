package com.spring.schoolmate.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EatPhotoService {

  // JPA 리포지토리 및 다른 서비스에 대한 의존성 주입을 위한 변수들
  private final EatPhotoRepository eatPhotoRepository;
  private final EatphotoVisionScoreRepository eatphotoVisionScoreRepository;
  private final VisionLabelRepository visionLabelRepository;
  private final StudentRepository studentRepository;
  private final TranslationService translationService; // 라벨 번역을 위한 서비스

  // 생성자를 통한 의존성 주입 (Constructor Injection)
  public EatPhotoService(
    EatPhotoRepository eatPhotoRepository,
    EatphotoVisionScoreRepository eatphotoVisionScoreRepository,
    VisionLabelRepository visionLabelRepository,
    StudentRepository studentRepository,
    TranslationService translationService) {
    this.eatPhotoRepository = eatPhotoRepository;
    this.eatphotoVisionScoreRepository = eatphotoVisionScoreRepository;
    this.visionLabelRepository = visionLabelRepository;
    this.studentRepository = studentRepository;
    this.translationService = translationService;
  }

  // 업로드된 사진을 분석하고 결과를 저장
  public String uploadAndAnalyzePhoto(byte[] photoData, Long studentId) throws Exception {
    // 1. 학생 ID로 학생 엔티티를 찾습니다. 없으면 예외를 던진다.
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new DMLException("학생의 고유 아이디를 찾지 못했습니다.: " + studentId));

    // 2. 새로운 급식 사진(EatPhoto) 엔티티를 생성하고 데이터베이스에 저장
    EatPhoto eatPhoto = new EatPhoto();
    eatPhoto.setStudent(student);
    eatPhoto.setEatimageUrl(null); // S3를 사용하지 않으므로 URL은 null로 설정
    eatPhoto.setEatuploadedAt(LocalDateTime.now());
    final EatPhoto savedEatPhoto = eatPhotoRepository.save(eatPhoto);

    // 3. Google Cloud Vision AI API를 호출하여 이미지 분석을 수행
    // try-with-resources 구문을 사용해 클라이언트 리소스를 자동으로 해제
    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
      // 이미지 바이트 데이터를 Google Cloud Vision API가 인식할 수 있는 형식으로 변환
      ByteString imgBytes = ByteString.copyFrom(photoData);
      Image image = Image.newBuilder().setContent(imgBytes).build();
      // 이미지에서 '라벨 감지' 기능을 요청
      Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feature)
        .setImage(image)
        .build();

      // Vision AI에 분석 요청을 보내고 응답
      BatchAnnotateImagesResponse response = vision.batchAnnotateImages(Collections.singletonList(request));
      AnnotateImageResponse annotateImageResponse = response.getResponses(0);

      // 4. 분석 결과를 처리하고 데이터베이스에 저장
      boolean isSchoolLunch = false;
      // AI가 감지한 라벨 목록을 순회
      for (EntityAnnotation label : annotateImageResponse.getLabelAnnotationsList()) {
        String englishLabelName = label.getDescription();
        Float score = label.getScore();

        // 라벨 이름과 점수를 기반으로 급식 사진 여부를 판단
        if ((englishLabelName.equalsIgnoreCase("Food") || englishLabelName.equalsIgnoreCase("Meal") || englishLabelName.equalsIgnoreCase("Cuisine")) && score > 0.80f) {
          isSchoolLunch = true;
        }

        // TranslationService를 이용해 영문 라벨을 한국어로 번역
        String koreanLabelName = translationService.translate(englishLabelName, "ko");

        // 데이터베이스에 번역된 라벨이 이미 존재하는지 확인하고, 없으면 새로 저장
        Optional<VisionLabel> existingLabel = visionLabelRepository.findByLabelName(koreanLabelName);
        VisionLabel visionLabel = existingLabel.orElseGet(() -> {
          return visionLabelRepository.save(
            VisionLabel.builder()
              .labelName(koreanLabelName)
              .languageCode("ko")
              .build()
          );
        });

        // 사진과 분석 결과를 연결하여 데이터베이스에 저장
        EatphotoVisionScore visionScore = EatphotoVisionScore.builder()
          .eatphoto(savedEatPhoto)
          .visionLabel(visionLabel)
          .score(score)
          .build();
        eatphotoVisionScoreRepository.save(visionScore);
      }

      // 5. 최종 급식 사진 여부에 따라 다른 결과를 반환
      if (isSchoolLunch) {
        return "급식 사진이 맞습니다.";
      } else {
        return "급식 사진이 아닙니다.";
      }
    }
  }

  // 특정 학생이 업로드한 모든 사진을 조회
  public List<EatPhoto> getPhotoByStudentId(Integer studentId) {
    return eatPhotoRepository.findByStudent_StudentId(studentId);
  }

  // 모든 학생이 업로드한 모든 사진을 조회
  public List<EatPhoto> getAllStudentPhotos() {
    return eatPhotoRepository.findAllStudentPhotos();
  }
}