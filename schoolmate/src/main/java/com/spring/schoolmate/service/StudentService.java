package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.ProfileRepository;
import com.spring.schoolmate.repository.RoleRepository;
import com.spring.schoolmate.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(Student student) {

    }

}
