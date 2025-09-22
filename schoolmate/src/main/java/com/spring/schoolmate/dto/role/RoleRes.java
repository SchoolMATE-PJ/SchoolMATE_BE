package com.spring.schoolmate.dto.role;

import com.spring.schoolmate.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRes {

    private Integer roleId;
    private Role.RoleType roleName;

    public static RoleRes fromEntity(Role role) {
        return RoleRes.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .build();
    }
}
