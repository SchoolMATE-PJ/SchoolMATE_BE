package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Allergy;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.StudentAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentAllergyRepository extends JpaRepository<StudentAllergy, Long> {

    List<StudentAllergy> findByAllergy(Allergy allergy);

    List<StudentAllergy> findByStudent(Student student);

    // 알레르기를 보유한 학생 검색
    @Query("SELECT sa.student FROM StudentAllergy sa WHERE sa.allergy = :allergy")
    List<Student> findStudnetsByAllergy(@Param("allergy")Allergy allergy);

    // 학생이 보유한 알레르기 검색
    @Query("SELECT sa.allergy FROM StudentAllergy sa WHERE sa.student = :student")
    List<Allergy> findAllergiesByStudent(@Param("student") Student student);

    // 특정 학생의 모든 알레르기 정보를 삭제하는 메소드
    void deleteAllByStudent(Student student);
}
