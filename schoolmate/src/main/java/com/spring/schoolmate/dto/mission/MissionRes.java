package com.spring.schoolmate.dto.mission;

import com.spring.schoolmate.entity.Mission;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRes {
    private Long mId;
    private String mTitle;
    private String mDetail;
    private Integer mPoint;
    private LocalDate startDate;
    private LocalDate endDate;

    public static MissionRes fromEntity(Mission mission) {
        return MissionRes.builder()
                .mId(mission.getMId())
                .mTitle(mission.getMTitle())
                .mDetail(mission.getMDetail())
                .mPoint(mission.getMPoint())
                .startDate(mission.getStartDate())
                .endDate(mission.getEndDate())
                .build();
    }

}
