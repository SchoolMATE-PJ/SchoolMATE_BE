package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // 전화번호 중복 체크
    boolean existsByPhone(String phone);

    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    // Student Entity의 PK가 Profile Entity의 PK
    Optional<Profile> findByStudentId(Long studentId);

    // 닉네임으로 학생 검색 :: Unique
    Optional<Profile> findByNickname(String nickname);

    // 휴대전화 번호로 학생 검색 :: Unique
    Optional<Profile> findByPhone(String phone);

}
