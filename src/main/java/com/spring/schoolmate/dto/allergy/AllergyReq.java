package com.spring.schoolmate.dto.allergy;

import com.spring.schoolmate.entity.Allergy;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 관리자 새로 등록, 수정 용
public class AllergyReq {

    private String allergyName;
    private Integer allergyNo;

    public Allergy toAllergy(AllergyReq allergyReq) {
        return Allergy.builder()
                .allergyName(allergyReq.getAllergyName())
                .allergyNo(allergyReq.getAllergyNo())
                .build();
    }

}
