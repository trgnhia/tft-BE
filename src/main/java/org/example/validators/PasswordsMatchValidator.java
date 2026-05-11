package org.example.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.annotations.PasswordsMatch;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, PasswordMatchable> {

    @Override
    public boolean isValid(PasswordMatchable request, ConstraintValidatorContext context) {
        if (request.getPasswordToMatch() == null || request.getConfirmPasswordToMatch() == null) {
            return false;
        }

        boolean isValid = request.getPasswordToMatch().equals(request.getConfirmPasswordToMatch());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(request.getConfirmFieldName())
                    .addConstraintViolation();
        }

        return isValid;
    }
}