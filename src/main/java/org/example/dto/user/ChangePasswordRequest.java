package org.example.dto.user;

import org.example.annotations.Password;
import org.example.annotations.PasswordsMatch;
import org.example.validators.PasswordMatchable;

@PasswordsMatch
public record ChangePasswordRequest(String oldPassword, @Password String newPassword,
                                    String confirmNewPassword) implements PasswordMatchable {
    @Override
    public String getPasswordToMatch() {
        return newPassword;
    }

    @Override
    public String getConfirmPasswordToMatch() {
        return confirmNewPassword;
    }

    @Override
    public String getConfirmFieldName() {
        return "confirmNewPassword";
    }
}
