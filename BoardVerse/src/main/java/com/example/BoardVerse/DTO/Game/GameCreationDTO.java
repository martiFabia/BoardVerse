package com.example.BoardVerse.DTO.Game;

import com.example.BoardVerse.utils.validation.annotations.NotBlankListElements;
import com.example.BoardVerse.utils.validation.annotations.ValidMaxPlayTime;
import com.example.BoardVerse.utils.validation.annotations.ValidMaxPlayers;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
@ValidMaxPlayers
@ValidMaxPlayTime
public class GameCreationDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank
    @Size(min = 2, max = 200, message = "Short description must be between 2 and 100 characters")
    private String shortDescription;

    @NotBlank
    @Size(min = 2, message = "Description must be at least 2 characters")
    private String description;

    @NotNull(message = "Year released cannot be blank")
    @PositiveOrZero(message = "Year released must be a positive integer (0 if unknown)")
    private Integer yearReleased;

    @NotNull(message = "Minimum number of players cannot be blank")
    @Positive(message = "Minimum number of players must be a positive integer")
    @Min(value = 1, message = "Minimum number of players must be at least 1")
    private Integer minPlayers;

    @Positive(message = "Maximum number of players must be a positive integer")
    private Integer maxPlayers;

    @NotNull(message = "Minimum suggested age cannot be blank")
    @Positive(message = "Minimum suggested age must be a positive integer")
    @Max(value = 99, message = "Minimum suggested age must be less than 100")
    private Integer minSuggAge;

    @NotNull(message = "Minimun play time cannot be blank")
    @Positive(message = "Minimum play time must be a positive integer")
    @Min(value = 1, message = "Minimum play time must be at least 1")
    private Integer minPlayTime;

    @NotNull(message = "Maximum play time cannot be blank")
    @Positive(message = "Maximum play time must be a positive integer")
    private Integer maxPlayTime;

    @NotBlankListElements
    private List<String> designers;

    @NotBlankListElements
    private List<String> artists;

    @NotBlankListElements
    private List<String> publisher;

    @NotNull(message = "Categories cannot be null")
    private List<String> categories;

    @NotBlankListElements
    private List<String> mechanics;

    @NotBlankListElements
    private List<String> family;
}
