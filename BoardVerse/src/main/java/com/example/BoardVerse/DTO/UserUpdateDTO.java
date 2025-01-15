package com.example.BoardVerse.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.util.Date;

public record UserUpdateDTO (
        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        String city,
        String country,
        String state,
        Date birthDate
) {
}
