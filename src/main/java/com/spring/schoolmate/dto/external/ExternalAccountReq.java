package com.spring.schoolmate.dto.external;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalAccountReq {
    private String providerId;
    private String providerName;
}
