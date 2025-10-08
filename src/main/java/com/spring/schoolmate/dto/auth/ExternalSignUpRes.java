package com.spring.schoolmate.dto.auth;

import com.spring.schoolmate.dto.allergy.AllergyRes;
import com.spring.schoolmate.dto.external.ExternalAccountRes;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.dto.student.StudentRes;
import com.spring.schoolmate.entity.Allergy;
import com.spring.schoolmate.entity.ExternalAccount;
import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Student;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalSignUpRes {

    private StudentRes student;
    private ProfileRes profile;
    private ExternalAccountRes externalAccount;
    private List<AllergyRes> allergies;
    private String token;

    public static ExternalSignUpRes fromEntity(Student student, Profile profile,
                                               ExternalAccount externalAccount,
                                               List<Allergy>allergies,
                                               String token) {
        return ExternalSignUpRes.builder()
                .student(StudentRes.fromEntity(student))
                .profile(ProfileRes.fromEntity(profile))
                .externalAccount(ExternalAccountRes.fromEntity(externalAccount))
                .allergies(allergies == null ? List.of()
                        : allergies.stream().map(AllergyRes::fromEntity).toList())
                .token(token)
                .build();
    }

}
