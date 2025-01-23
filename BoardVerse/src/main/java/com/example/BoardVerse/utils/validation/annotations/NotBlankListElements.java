package com.example.BoardVerse.utils.validation.annotations;

import com.example.BoardVerse.utils.validation.NotBlankListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotBlankListValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankListElements {
    String message() default "List elements must not be blank or null (but list can be empty)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}