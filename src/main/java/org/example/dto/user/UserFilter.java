package org.example.dto.user;

public record UserFilter(String userName, String email, Long roleId, Boolean enabled) {
}
