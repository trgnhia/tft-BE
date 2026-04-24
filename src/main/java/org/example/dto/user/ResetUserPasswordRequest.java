package org.example.dto.user;

import org.example.annotations.Password;
import org.example.annotations.PasswordsMatch;

@PasswordsMatch
public record ResetUserPasswordRequest(@Password String newPassword, String confirmNewPassword) {
}
