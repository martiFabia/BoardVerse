package com.example.BoardVerse.utils.validation;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.utils.validation.annotations.ValidMaxPlayTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxPlayTimeValidator implements ConstraintValidator<ValidMaxPlayTime, GameCreationDTO> {

    @Override
    public void initialize(ValidMaxPlayTime constraintAnnotation) {
    }

    @Override
    public boolean isValid(GameCreationDTO gameCreationDTO, ConstraintValidatorContext context) {
        if (gameCreationDTO.getMaxPlayTime() == null || gameCreationDTO.getMinPlayTime() == null) {
            return true; // Skip validation if either value is null
        }
        return gameCreationDTO.getMaxPlayTime() >= gameCreationDTO.getMinPlayTime();
    }
}