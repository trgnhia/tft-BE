package org.example.validators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

public interface PasswordMatchable {
    @Schema(hidden = true)
    @JsonIgnore
    String getPasswordToMatch();

    @Schema(hidden = true)
    @JsonIgnore
    String getConfirmPasswordToMatch();

    @Schema(hidden = true)
    @JsonIgnore
    String getConfirmFieldName();
}