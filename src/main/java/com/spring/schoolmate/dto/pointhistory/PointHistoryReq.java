package com.spring.schoolmate.dto.pointhistory;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 관리자가 수동으로 포인트 지급/차감하는 경우 사용
public class PointHistoryReq {

    private Long studentId;
    private String tsType;
    private Integer amount;
    private String refType;
    private Long refId;
}
