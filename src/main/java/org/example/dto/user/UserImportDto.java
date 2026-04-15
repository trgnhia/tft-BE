package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.annotations.ImportColumn;
import org.example.annotations.Password;
import org.example.common.constant.Constants;

import static org.example.common.constant.Constants.MessageKey.USER_IMPORT_EMAIL_INVALID;
import static org.example.common.constant.Constants.MessageKey.USER_IMPORT_EMAIL_REQUIRED;
import static org.example.common.constant.Constants.MessageKey.USER_IMPORT_ENABLED_REQUIRED;
import static org.example.common.constant.Constants.MessageKey.USER_IMPORT_PASSWORD_REQUIRED;
import static org.example.common.constant.Constants.MessageKey.USER_IMPORT_ROLE_ID_REQUIRED;
import static org.example.common.constant.Constants.MessageKey.USER_IMPORT_USERNAME_REQUIRED;
import static org.example.common.constant.Constants.MessageKey.USER_IMPORT_USERNAME_SIZE;

@Getter
@Setter
@NoArgsConstructor
public class UserImportDto {
    @ImportColumn(name = "Username", required = true)
    @NotBlank(message = "{" + Constants.MessageKey.USER_IMPORT_USERNAME_REQUIRED + "}")
    @Size(min = 3, max = 50, message = USER_IMPORT_USERNAME_SIZE)
    private String username;

    @ImportColumn(name = "Email", required = true)
    @NotBlank(message = USER_IMPORT_EMAIL_REQUIRED)
    @Email(message = USER_IMPORT_EMAIL_INVALID)
    private String email;

    @ImportColumn(name = "Password", required = true)
    @NotBlank(message = USER_IMPORT_PASSWORD_REQUIRED)
    @Password
    private String password;

    @ImportColumn(name = "Role Id", required = true)
    @NotNull(message = USER_IMPORT_ROLE_ID_REQUIRED)
    private Long roleId;

    @ImportColumn(name = "Enabled", required = true)
    @NotNull(message = USER_IMPORT_ENABLED_REQUIRED)
    private Boolean enabled;
}
