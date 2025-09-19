package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    // 아직 비즈니스 요구 없음
}
