package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Allergy;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.StudentAllergy;
import com.spring.schoolmate.repository.AllergyRepository;
import com.spring.schoolmate.repository.StudentAllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AllergyService {

    private final AllergyRepository allergyRepository;
    private final StudentAllergyRepository studentAllergyRepository;

    /**
     * 학생의 알레르기 정보를 등록
     * @param student 알레르기 정보를 등록할 학생 엔티티
     * @param allergyIds 등록할 알레르기 ID 목록
     * @return 등록된 알레르기 엔티티 목록
     */
    @Transactional
    public List<Allergy> registerStudentAllergies(Student student, List<Integer> allergyIds) {
        if (allergyIds == null || allergyIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Allergy> allergies = allergyRepository.findAllById(allergyIds);
        List<StudentAllergy> studentAllergies = allergies.stream()
                .map(allergy -> new StudentAllergy(student, allergy))
                .collect(Collectors.toList());
        studentAllergyRepository.saveAll(studentAllergies);
        return allergies;
    }
}
