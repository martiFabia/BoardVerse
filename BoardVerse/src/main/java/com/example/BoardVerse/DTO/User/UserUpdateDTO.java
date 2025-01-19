package com.example.BoardVerse.DTO.User;

import com.example.BoardVerse.model.MongoDB.subentities.Location;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record UserUpdateDTO (

        @Schema(example = "null")
        String username,
        @Schema(example = "null")
        String email,
        @Schema(example = "null")
        String firstName,
        @Schema(example = "null")
        String lastName,
        @Schema(example = "null")
        String password,
        @Schema(description = "User Location (Country, State, City)",
                example = "{ \"country\": \"null\", \"state\": \"null\", \"city\": \"null\" }")
        Location location,
        @Schema(example = "null")
        Date birthDate
) {
}
