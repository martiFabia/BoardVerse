package com.example.BoardVerse.utils.validation.annotations;

import com.example.BoardVerse.utils.validation.MaxPlayersValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MaxPlayersValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMaxPlayers {
    String message() default "Maximum number of players must be greater than or equal to minimum number of players";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}