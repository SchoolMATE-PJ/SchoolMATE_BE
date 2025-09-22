package com.spring.schoolmate.dto.auth;

import com.spring.schoolmate.dto.allergy.AllergyRes;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.dto.student.StudentRes;
import com.spring.schoolmate.entity.Allergy;
import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Student;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRes {

    private StudentRes student;
    private ProfileRes profile;
    private List<AllergyRes> allergies;

    public static SignUpRes fromEntity(Student student, Profile profile, List<Allergy> allergies) {
        return SignUpRes.builder()
                .student(StudentRes.fromEntity(student))
                .profile(ProfileRes.fromEntity(profile))
                .allergies(allergies == null ? List.of()
                        : allergies.stream().map(AllergyRes::fromEntity).toList())
                .build();
    }
}
