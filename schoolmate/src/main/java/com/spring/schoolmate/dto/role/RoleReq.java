package com.spring.schoolmate.dto.role;

import com.spring.schoolmate.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleReq {

    private Role.RoleType roleType;
}
