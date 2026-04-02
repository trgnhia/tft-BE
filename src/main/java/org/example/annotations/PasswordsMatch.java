package org.example.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.validators.PasswordsMatchValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {
    String message() default "error.PASSWORD_MISMATCH";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
