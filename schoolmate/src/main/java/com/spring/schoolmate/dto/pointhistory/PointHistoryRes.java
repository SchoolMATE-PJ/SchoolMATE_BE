package com.spring.schoolmate.dto.pointhistory;

import com.spring.schoolmate.entity.PointHistory;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryRes {

    private Long phId;
    private Long studentId;
    private String tsType;
    private Integer amount;
    private Integer balanceAfter;
    private String refType;
    private Long refId;
    private Timestamp createdAt;

    public static PointHistoryRes fromEntity(PointHistory pointHistory) {
        return PointHistoryRes.builder()
                .phId(pointHistory.getPhId())
                .studentId(pointHistory.getStudent().getStudentId())
                .tsType(pointHistory.getTsType())
                .amount(pointHistory.getAmount())
                .balanceAfter(pointHistory.getBalanceAfter())
                .refType(pointHistory.getRefType())
                .refId(pointHistory.getRefId())
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }
}
