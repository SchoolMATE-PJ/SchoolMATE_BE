package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    // 회원 가입 시 권한 부여
    Optional<Role> findByRoleName(Role.RoleType roleName);
}
