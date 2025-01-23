package com.example.BoardVerse.utils.validation;

import com.example.BoardVerse.utils.validation.annotations.NotBlankListElements;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class NotBlankListValidator implements ConstraintValidator<NotBlankListElements, List<String>> {

    @Override
    public void initialize(NotBlankListElements constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Skip validation if the list is null
        }
        for (String element : value) {
            if (StringUtils.isBlank(element)) {
                return false;
            }
        }
        return true;
    }
}