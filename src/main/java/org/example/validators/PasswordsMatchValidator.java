package org.example.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.annotations.PasswordsMatch;
import org.example.dto.auth.SignUpRequest;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, SignUpRequest> {

    @Override
    public boolean isValid(SignUpRequest request, ConstraintValidatorContext context) {
        if (request.password() == null || request.confirmPassword() == null) {
            return false;
        }

        boolean isValid = request.password().equals(request.confirmPassword());

        // Custom lại để lỗi hiển thị trực tiếp ở field "confirmPassword" thay vì lỗi chung của class
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}