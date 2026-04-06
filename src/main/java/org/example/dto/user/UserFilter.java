package org.example.dto.user;

import org.example.common.enums.RoleCode;

public record UserFilter(RoleCode roleCode, Boolean enable) {
}
