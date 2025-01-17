package com.example.BoardVerse.DTO.Game;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class GameInfoDTO {

    private String name;

    private String description;

    private String shortDescription;

    private String imgURL;

    private double averageRating;

    private int numberReviews;

    private int yearReleased;

    private int minPlayers;

    private int maxPlayers;

    private int minSuggAge;

    private int minPlayTime;

    private int maxPlayTime;

    private List<String> designers;

    private List<String> artists;

    private List<String> publisher;

    private List<String> categories;

    private List<String> mechanics;

}
