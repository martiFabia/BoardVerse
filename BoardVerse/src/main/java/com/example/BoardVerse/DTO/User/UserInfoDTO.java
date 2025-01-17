package com.example.BoardVerse.DTO.User;

import com.example.BoardVerse.DTO.Review.ReviewInfoDTO;
import com.example.BoardVerse.DTO.Review.ReviewUser;
import com.example.BoardVerse.model.MongoDB.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.util.Date;
import java.util.List;

public record UserInfoDTO (
    @NotBlank String username,
    @NotBlank String email,
    String firstName,
    String lastName,
    Location location,
    @Past Date birthDate,
    List<ReviewUser> mostRecentReviews
){
}
