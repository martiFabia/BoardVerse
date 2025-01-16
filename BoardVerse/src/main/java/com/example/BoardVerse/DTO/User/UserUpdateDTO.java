package com.example.BoardVerse.DTO.User;

import com.example.BoardVerse.model.MongoDB.Location;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record UserUpdateDTO (

        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        @Schema(description = "User Location (Country, State, City)",
                example = "{ \"country\": \"Italy\", \"state\": \"Tuscany\", \"city\": \"Pisa\" }")
        Location location,
        Date birthDate
) {
}
