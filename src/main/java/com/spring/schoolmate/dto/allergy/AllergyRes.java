package com.spring.schoolmate.dto.allergy;

import com.spring.schoolmate.entity.Allergy;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyRes {
    private Integer allergyId;
    private String allergyName;
    private Integer allergyNo;

    public static AllergyRes fromEntity(Allergy allergy) {
        return AllergyRes.builder()
                .allergyId(allergy.getAllergyId())
                .allergyName(allergy.getAllergyName())
                .allergyNo(allergy.getAllergyNo())
                .build();
    }
}
