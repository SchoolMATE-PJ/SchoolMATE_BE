package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllergyRepository extends JpaRepository<Allergy, Integer> {

    // 이름으로 조회
    Optional<Allergy> findByAllergyName(String allergyName);

    // 번호로 조회
    Optional<Allergy> findByAllergyNo(Integer allergyNo);
}
