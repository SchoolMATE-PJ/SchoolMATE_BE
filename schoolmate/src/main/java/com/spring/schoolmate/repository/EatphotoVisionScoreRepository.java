package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.EatphotoVisionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EatphotoVisionScoreRepository extends JpaRepository<EatphotoVisionScore, Integer> {

  // 추가적인 비즈니스 로직에 필요한 메서드는 여기에 정의 가능
}