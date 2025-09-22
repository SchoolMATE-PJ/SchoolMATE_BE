package com.spring.schoolmate.dto.auth;

import com.spring.schoolmate.dto.external.ExternalAccountReq;
import com.spring.schoolmate.dto.profile.ProfileReq;
import com.spring.schoolmate.dto.student.StudentReq;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalSignUpReq {
    private StudentReq student;
    private ProfileReq profile;
    private List<Integer> allergyId;
    private ExternalAccountReq externalAccount;
}
