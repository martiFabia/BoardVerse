package com.example.BoardVerse.DTO.User;
import com.example.BoardVerse.model.MongoDB.subentities.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Date;


public record UserRegDTO(
        @NotBlank
        String username,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        @Size(min = 3)
        String password,
        @Schema(description = "UserMongo Location (Country, State, City)",
                example = "{ \"country\": \"Italy\", \"state\": \"Tuscany\", \"city\": \"Pisa\" }")
        Location location,
        @Past
        Date birthDate
){}



/*
public class SignupRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 2, max = 40, message = "Password must be between 2 and 40 characters")
    private String password;

    // Getters e Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

 */
