package org.example.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.validators.PasswordValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "error.WEAK_PASSWORD";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
