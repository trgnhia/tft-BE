package org.example.dto.auth;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.NonNull;
import org.example.annotations.Password;
import org.example.annotations.PasswordsMatch;

@Builder
@PasswordsMatch
public record SignUpRequest(@NonNull String username, @NonNull @Email String email,
                            @Password String password, String confirmPassword) {
}
