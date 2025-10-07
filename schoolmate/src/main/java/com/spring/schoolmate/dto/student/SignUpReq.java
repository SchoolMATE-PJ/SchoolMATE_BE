package com.spring.schoolmate.dto.student;

import com.spring.schoolmate.entity.Profile;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SignUpReq {
    // Student
    private String email;
    private String password;
    private String name;

    // Profile
    private String nickname;
    private Profile.Gender gender;
    private String phone;
    private LocalDate birthDay;
    private String scCode;
    private String schoolCode;
    private String schoolName;
    private String majorName;
    private String level;
    private int grade;
    private int classNo;

    // 알레르기 정보
    private List<Integer> allergyId;
}
