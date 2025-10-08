package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.VisionLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VisionLabelRepository extends JpaRepository<VisionLabel, Integer> {

  /**
   * 라벨명(labelName)을 기준으로 VisionLabel 엔티티를 조회
   * Google Cloud Vision AI 활용
   * Optional을 사용하여 해당 라벨이 존재하지 않을 경우를 안전하게 처리
   * @param labelName AI가 식별한 라벨명 (예: "김치", "밥")
   * @return VisionLabel 엔티티를 담고 있는 Optional 객체
   */
  Optional<VisionLabel> findByLabelName(String labelName);
}