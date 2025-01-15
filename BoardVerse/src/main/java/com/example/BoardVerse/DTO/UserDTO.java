package com.example.BoardVerse.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UserDTO(
    @NotBlank
    String username,
    @NotBlank
    @Email
    String email
) {

}
