package com.example.BoardVerse.DTO.Game;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class GameUpdateDTO {

    //@NotBlank(message = "Name is mandatory")
    @NotEmpty
    private String name;

    @NotEmpty
    private String description;
    @NotEmpty
    private String shortDescription;

    //@Positive(message = "Average rating must be positive")
    //private double averageRating;

    //@Positive(message = "Number of reviews must be positive")
    //private int numberReviews;

    //@NotNull(message = "Year released is mandatory")
    @Positive(message = "Year released must be a positive integer")
    //@Min(value = 1900, message = "Year released must be after 1900")
    //@Max(value = 2024, message = "Year released must be before 2024")
    @NotEmpty
    private Integer yearReleased;

   // @Positive(message = "Minimum number of players must be a positive integer")
    //@Min(value = 1, message = "Minimum number of players must be at least 1")
    @NotEmpty
    private Integer minPlayers;
    //@Positive(message = "Maximum number of players must be a positive integer")
    @NotEmpty
    private Integer maxPlayers;

    //@Positive(message = "Minimum suggested age must be a positive integer")
    @Max(value = 99, message = "Minimum suggested age must be less than 99")
    @NotEmpty
    private Integer minSuggAge;

    //@Positive(message = "Minimum play time must be a positive integer")
    @Min(value = 1, message = "Minimum play time must be at least 1")
    @NotEmpty
    private Integer minPlayTime;

    //@Positive(message = "Maximum play time must be a positive integer")
    @NotEmpty
    private Integer maxPlayTime;
    @NotEmpty
    private List<String> designers;
    @NotEmpty
    private List<String> artists;
    @NotEmpty
    private List<String> publisher;
    @NotEmpty
    private List<String> categories;
    @NotEmpty
    private List<String> mechanics;
    @NotEmpty
    private List<String> family;
}
