package com.spring.schoolmate.dto.external;

import com.spring.schoolmate.entity.ExternalAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "외부 계정 정보 응답 DTO")
public class ExternalAccountRes {

    private Long studentId;
    private String providerName;
    private String providerId;

    public static ExternalAccountRes fromEntity(ExternalAccount externalAccount) {
        return ExternalAccountRes.builder()
                .studentId(externalAccount.getStudent().getStudentId())
                .providerName(externalAccount.getProviderName())
                .providerId(externalAccount.getProviderId())
                .build();
    }

}
