package com.example.BoardVerse.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.util.Date;

public record UserInfoDTO (
    @NotBlank String username,
    @NotBlank String email,
    String firstName,
    String lastName,
    String city,
    String country,
    String state,
    @Past Date birthDate
){
}
