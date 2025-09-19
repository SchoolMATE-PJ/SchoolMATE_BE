package com.spring.schoolmate.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.spring.schoolmate.entity.EatPhoto;
import com.spring.schoolmate.entity.EatphotoVisionScore;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.VisionLabel;
import com.spring.schoolmate.repository.EatPhotoRepository;
import com.spring.schoolmate.repository.EatphotoVisionScoreRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.repository.VisionLabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
public class EatPhotoService {

  private final EatPhotoRepository eatPhotoRepository;
  private final EatphotoVisionScoreRepository eatphotoVisionScoreRepository;
  private final VisionLabelRepository visionLabelRepository;
  private final StudentRepository studentRepository;

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

  public void uploadAndAnalyzePhoto(byte[] photoData, Long studentId, String imageUrl) throws Exception {
    // 1. Student 엔티티를 찾아서 연결
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new IllegalArgumentException("학생의 고유 아이디를 찾지 못했습니다.: " + studentId));

    EatPhoto eatPhoto = new EatPhoto();
    eatPhoto.setStudent(student);
    eatPhoto.setEatimageUrl(imageUrl);
    eatPhoto.setEatuploadedAt(LocalDateTime.now());

    // 2. EatPhoto 엔티티를 저장하고 반환 값을 final 변수에 할당
    final EatPhoto savedEatPhoto = eatPhotoRepository.save(eatPhoto);

    // 3. Google Cloud Vision AI API 호출
    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
      ByteString imgBytes = ByteString.copyFrom(photoData);
      Image image = Image.newBuilder().setContent(imgBytes).build();
      Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feature)
        .setImage(image)
        .build();

      BatchAnnotateImagesResponse response = vision.batchAnnotateImages(Collections.singletonList(request));
      AnnotateImageResponse annotateImageResponse = response.getResponses(0);

      // 4. 분석 결과 처리 및 데이터베이스에 저장
      annotateImageResponse.getLabelAnnotationsList().forEach(label -> {
        String labelName = label.getDescription();
        Float score = label.getScore();

        Optional<VisionLabel> existingLabel = visionLabelRepository.findByLabelName(labelName);
        VisionLabel visionLabel = existingLabel.orElseGet(() -> {
          return visionLabelRepository.save(
            VisionLabel.builder()
              .labelName(labelName)
              .languageCode("ko")
              .build()
          );
        });

        EatphotoVisionScore visionScore = EatphotoVisionScore.builder()
          .eatphoto(savedEatPhoto) // 'savedEatPhoto' 변수 사용
          .visionLabel(visionLabel)
          .score(score)
          .build();
        eatphotoVisionScoreRepository.save(visionScore);
      });
    }
  }
}