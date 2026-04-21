package org.example.dto.user;

public record UserFilter(String username, String email, Long roleId, Boolean enabled) {
}
