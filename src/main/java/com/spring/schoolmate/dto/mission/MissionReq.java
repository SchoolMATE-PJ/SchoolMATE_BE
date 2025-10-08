package com.spring.schoolmate.dto.mission;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionReq {
    private String mTitle;
    private String mDetail;
    private Integer mPoint;
    private LocalDate startDate;
    private LocalDate endDate;
}
