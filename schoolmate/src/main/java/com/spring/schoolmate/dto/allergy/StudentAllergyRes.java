package com.spring.schoolmate.dto.allergy;

import com.spring.schoolmate.entity.StudentAllergy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAllergyRes {

    private Integer saId;
    private Long studentId;
    private AllergyRes allergy;

    public static StudentAllergyRes fromEntity(StudentAllergy studentAllergy) {
        return StudentAllergyRes.builder()
                .saId(studentAllergy.getSaId())
                .studentId(studentAllergy.getStudent().getStudentId())
                .allergy(AllergyRes.fromEntity(studentAllergy.getAllergy()))
                .build();
    }
}
