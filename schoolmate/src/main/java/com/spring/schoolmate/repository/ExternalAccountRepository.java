package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.ExternalAccount;
import com.spring.schoolmate.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, Long> {

    // 소셜 계정 고유 ID로 찾기
    Optional<ExternalAccount> findByProviderId(String providerId);

    // 특정 학생이 가진 모든 소셜 계정 찾기
    List<ExternalAccount> findByStudent(Student student);

    // 특정 소셜 서비스로 가입한 모든 학생 조회
    @Query("SELECT ea.student FROM ExternalAccount ea WHERE ea.providerName = :providerName")
    List<Student> findStudentsByProviderName(@Param("providerName") String providerName);

    // 소셜 서비스명 + providerId 조합으로 찾기
    Optional<ExternalAccount> findByProviderNameAndProviderId(String providerName, String providerId);

    // 특정 소셜 서비스 가입자 수 카운트
    Long countByProviderName(String providerName);
}
