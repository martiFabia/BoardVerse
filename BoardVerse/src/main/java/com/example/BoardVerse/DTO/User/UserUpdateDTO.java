package com.example.BoardVerse.DTO.User;

import com.example.BoardVerse.model.MongoDB.subentities.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Date;

public record UserUpdateDTO (

        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        Location location,
        Date birthDate
) {
}
