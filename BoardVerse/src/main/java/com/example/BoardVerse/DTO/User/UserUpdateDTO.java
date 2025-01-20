package com.example.BoardVerse.DTO.User;

import com.example.BoardVerse.model.MongoDB.subentities.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Date;

public record UserUpdateDTO (

        @NotEmpty
        String username,
        @NotEmpty
        String email,
        @NotEmpty
        String firstName,
        @NotEmpty
        String lastName,
        @NotEmpty
        String password,
        @Schema(description = "User Location (Country, State, City)",
                example = "{ \"country\": \"null\", \"state\": \"null\", \"city\": \"null\" }")
        Location location,
        @NotEmpty
        Date birthDate
) {
}
