package com.example.BoardVerse.utils.validation.annotations;

import com.example.BoardVerse.utils.validation.MaxPlayTimeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MaxPlayTimeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMaxPlayTime {
    String message() default "Maximum play time must be greater than or equal to minimum play time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}