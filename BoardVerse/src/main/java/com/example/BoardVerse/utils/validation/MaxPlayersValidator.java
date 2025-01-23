package com.example.BoardVerse.utils.validation;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.utils.validation.annotations.ValidMaxPlayers;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxPlayersValidator implements ConstraintValidator<ValidMaxPlayers, GameCreationDTO> {

    @Override
    public void initialize(ValidMaxPlayers constraintAnnotation) {
    }

    @Override
    public boolean isValid(GameCreationDTO gameCreationDTO, ConstraintValidatorContext context) {
        if (gameCreationDTO.getMaxPlayers() == null || gameCreationDTO.getMinPlayers() == null) {
            return true; // Skip validation if either value is null
        }
        return gameCreationDTO.getMaxPlayers() >= gameCreationDTO.getMinPlayers();
    }
}