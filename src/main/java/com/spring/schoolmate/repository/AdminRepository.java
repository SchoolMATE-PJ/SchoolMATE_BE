package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

  // AdminInitializer 및 JWTFilter에서 Admin 계정 조회 시 사용
  Optional<Admin> findByEmail(String email);
}