package com.spring.schoolmate.service;

// Google Cloud Vision AI 관련 라이브러리 임포트
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

// 프로젝트 엔티티 및 예외 클래스 임포트
import com.spring.schoolmate.entity.EatPhoto;
import com.spring.schoolmate.entity.EatphotoVisionScore;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.VisionLabel;
import com.spring.schoolmate.exception.DMLException;

// 프로젝트 리포지토리(데이터베이스 접근) 임포트
import com.spring.schoolmate.repository.EatPhotoRepository;
import com.spring.schoolmate.repository.EatphotoVisionScoreRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.repository.VisionLabelRepository;

// Spring Framework 핵심 어노테이션 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Java 표준 라이브러리 임포트
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

// 이 클래스가 Spring의 서비스 레이어 컴포넌트임을 명시
@Service
// 메서드 실행 시 트랜잭션 관리를 적용하도록 명시
@Transactional
public class EatPhotoService {

  // JPA 리포지토리들을 private final 변수로 선언하고 의존성 주입을 받을 준비
  private final EatPhotoRepository eatPhotoRepository;
  private final EatphotoVisionScoreRepository eatphotoVisionScoreRepository;
  private final VisionLabelRepository visionLabelRepository;
  private final StudentRepository studentRepository;

  // 생성자를 통해 의존성 주입 (Constructor Injection)
  public EatPhotoService(
    EatPhotoRepository eatPhotoRepository,
    EatphotoVisionScoreRepository eatphotoVisionScoreRepository,
    VisionLabelRepository visionLabelRepository,
    StudentRepository studentRepository) {
    this.eatPhotoRepository = eatPhotoRepository;
    this.eatphotoVisionScoreRepository = eatphotoVisionScoreRepository;
    this.visionLabelRepository = visionLabelRepository;
    this.studentRepository = studentRepository;
  }

  /**
   * 급식 사진을 업로드하고 Google Cloud Vision AI를 통해 분석하는 핵심 메서드
   * @param photoData 업로드된 사진의 바이트 데이터
   * @param studentId 사진을 업로드한 학생의 고유 ID
   * @param imageUrl 업로드된 사진의 URL
   * @return 사진이 급식 사진일 경우 "급식 사진이 맞습니다." 아닐 경우 "급식 사진이 아닙니다." 반환
   * @throws Exception 예외 발생 시 던져짐
   */
  public String uploadAndAnalyzePhoto(byte[] photoData, Long studentId, String imageUrl) throws Exception {
    // 1. Student 엔티티를 찾아서 연결
    // studentId로 학생을 찾고, 없을 경우 DMLException 발생
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new DMLException("학생의 고유 아이디를 찾지 못했습니다.: " + studentId));

    // 새로운 EatPhoto 엔티티 생성 및 데이터 설정
    EatPhoto eatPhoto = new EatPhoto();
    eatPhoto.setStudent(student); // 위에서 찾은 학생 엔티티 연결
    eatPhoto.setEatimageUrl(imageUrl); // 이미지 URL 설정
    eatPhoto.setEatuploadedAt(LocalDateTime.now()); // 현재 시간 설정

    // 2. EatPhoto 엔티티를 저장하고 반환 값을 final 변수에 할당
    // 데이터베이스에 저장하고, 영속성 컨텍스트에 의해 관리되는 객체(savedEatPhoto)를 반환받음
    final EatPhoto savedEatPhoto = eatPhotoRepository.save(eatPhoto);

    // 3. Google Cloud Vision AI API 호출
    // try-with-resources 구문으로 ImageAnnotatorClient를 생성하여 리소스 자동 해제
    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
      // 이미지 바이트 데이터를 ByteString으로 변환하여 AI에 전달할 수 있는 형식으로 만듦
      ByteString imgBytes = ByteString.copyFrom(photoData);
      // Vision API 요청을 위한 Image 객체 생성
      Image image = Image.newBuilder().setContent(imgBytes).build();
      // 수행할 AI 분석 기능(라벨 감지)을 설정
      Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
      // 요청 객체 생성: 어떤 이미지에 어떤 기능을 적용할지 설정
      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feature)
        .setImage(image)
        .build();

      // 실제 Vision AI에 분석 요청을 보내고 응답을 받음
      BatchAnnotateImagesResponse response = vision.batchAnnotateImages(Collections.singletonList(request));
      // 응답 리스트에서 첫 번째 응답(우리가 요청한 이미지에 대한)을 가져옴
      AnnotateImageResponse annotateImageResponse = response.getResponses(0);

      // ✨ 급식 사진 판별 로직 추가
      // 플래그 변수를 false로 초기화
      boolean isSchoolLunch = false;
      // AI 분석 결과로 받은 라벨 목록을 순회
      for (EntityAnnotation label : annotateImageResponse.getLabelAnnotationsList()) {
        String labelName = label.getDescription();
        Float score = label.getScore();

        // 'Food', 'Meal', 'Cuisine' 등 음식 관련 라벨이면서 점수가 0.80 이상인지 확인
        if ((labelName.equalsIgnoreCase("Food") || labelName.equalsIgnoreCase("Meal") || labelName.equalsIgnoreCase("Cuisine")) && score > 0.80f) {
          isSchoolLunch = true; // 조건에 맞는 라벨을 찾으면 플래그를 true로 변경
        }

        // 분석 결과 처리 및 데이터베이스에 저장
        // 라벨 이름으로 VisionLabel이 이미 존재하는지 조회
        Optional<VisionLabel> existingLabel = visionLabelRepository.findByLabelName(labelName);
        // 존재하면 기존 엔티티를, 없으면 새로 생성하여 저장
        VisionLabel visionLabel = existingLabel.orElseGet(() -> {
          return visionLabelRepository.save(
            VisionLabel.builder()
              .labelName(labelName)
              .languageCode("ko") // 라벨 언어 코드를 한국어로 설정
              .build()
          );
        });

        // 급식 사진과 AI 분석 결과(라벨, 점수)를 연결하여 저장
        EatphotoVisionScore visionScore = EatphotoVisionScore.builder()
          .eatphoto(savedEatPhoto)
          .visionLabel(visionLabel)
          .score(score)
          .build();
        eatphotoVisionScoreRepository.save(visionScore);
      }

      // 최종 판별 결과에 따라 다른 문자열 반환
      if (isSchoolLunch) {
        return "급식 사진이 맞습니다.";
      } else {
        return "급식 사진이 아닙니다.";
      }
    }
  }
}