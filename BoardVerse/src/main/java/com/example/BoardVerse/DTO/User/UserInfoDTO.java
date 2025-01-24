package com.example.BoardVerse.DTO.User;

import com.example.BoardVerse.model.MongoDB.subentities.ReviewUser;
import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.subentities.TournamentsUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.util.Date;
import java.util.List;

public record UserInfoDTO (

        String id,
    @NotBlank String username,
    @NotBlank String email,
    String firstName,
    String lastName,
    Date registeredDate,
    Location location,
    @Past Date birthDate,
    int followers,
    int following,
    TournamentsUser tournaments,
    List<ReviewUser> mostRecentReviews
){
}
