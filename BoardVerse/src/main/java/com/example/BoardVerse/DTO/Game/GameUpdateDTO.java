package com.example.BoardVerse.DTO.Game;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class GameUpdateDTO {

    private String name;
    private String description;
    private String shortDescription;

    @Min(1900)
    private Integer yearReleased;

   // @Positive(message = "Minimum number of players must be a positive integer")
    //@Min(value = 1, message = "Minimum number of players must be at least 1")
    @Min(1)
    private Integer minPlayers;
    @Positive(message = "Maximum number of players must be a positive integer")
    private Integer maxPlayers;

    @Positive(message = "Minimum suggested age must be a positive integer")
    @Max(99)
    private Integer minSuggAge;

    //@Positive(message = "Minimum play time must be a positive integer")
    @Min(1)
    private Integer minPlayTime;
    @Positive(message = "Maximum play time must be a positive integer")
    private Integer maxPlayTime;

    private List<String> designers;
    private List<String> artists;
    private List<String> publisher;
    private List<String> categories;
    private List<String> mechanics;
    private List<String> family;


    public Integer getYearReleased() {
        return this.yearReleased = yearReleased;
    }
}
