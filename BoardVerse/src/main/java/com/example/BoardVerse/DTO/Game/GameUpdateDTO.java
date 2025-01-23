package com.example.BoardVerse.DTO.Game;

import com.example.BoardVerse.utils.validation.annotations.NotBlankListElements;
import com.example.BoardVerse.utils.validation.annotations.ValidMaxPlayTime;
import com.example.BoardVerse.utils.validation.annotations.ValidMaxPlayers;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class GameUpdateDTO {

    @Size(max = 50, message = "Name must be 50 characters or less")
    private String name;

    @Size(max = 200, message = "Short description must be 200 characters or less")
    private String shortDescription;

    private String description;

    @Positive(message = "Year released must be a positive integer (0 if unknown)")
    private Integer yearReleased;

    @Positive(message = "Minimum number of players must be a positive integer")
    @Min(value = 1, message = "Minimum number of players must be at least 1")
    private Integer minPlayers;

    @Positive(message = "Maximum number of players must be a positive integer")
    private Integer maxPlayers;

    @Positive(message = "Minimum suggested age must be a positive integer")
    @Max(value = 99, message = "Minimum suggested age must be less than 100")
    private Integer minSuggAge;

    @Positive(message = "Minimum play time must be a positive integer")
    @Min(value = 1, message = "Minimum play time must be at least 1")
    private Integer minPlayTime;

    @Positive(message = "Maximum play time must be a positive integer")
    private Integer maxPlayTime;

    @NotBlankListElements
    private List<String> designers;

    @NotBlankListElements
    private List<String> artists;

    @NotBlankListElements
    private List<String> publisher;

    private List<String> categories;

    @NotBlankListElements
    private List<String> mechanics;

    @NotBlankListElements
    private List<String> family;

}
