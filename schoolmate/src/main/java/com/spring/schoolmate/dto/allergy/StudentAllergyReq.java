package com.spring.schoolmate.dto.allergy;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAllergyReq {
    private Long studentId;
    private Integer allergyId;

}
