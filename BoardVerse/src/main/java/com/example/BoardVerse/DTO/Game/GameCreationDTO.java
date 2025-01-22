package com.example.BoardVerse.DTO.Game;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

//import com.example.BoardVerse.utils.validation.ValidMaxPlayers;
//import com.example.BoardVerse.utils.validation.ValidMaxPlayTime;

@Data
public class GameCreationDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank
    private String shortDescription;

    @NotBlank
    private String description;

    @NotNull(message = "Year released cannot be blank")
    @Positive(message = "Year released must be a positive integer")
    @Min(value = 1900, message = "Year released must be after 1900")
    private Integer yearReleased;

    @NotNull(message = "Minimum number of players cannot be blank")
    @Positive(message = "Minimum number of players must be a positive integer")
    @Min(1)
    private Integer minPlayers;

    @NotNull(message = "Maximum number of players cannot be blank")
    @Positive(message = "Maximum number of players must be a positive integer")
    //@ValidMaxPlayers
    private Integer maxPlayers;

    @NotNull(message = "Minimun suggested age cannot be blank")
    @Positive(message = "Minimum suggested age must be a positive integer")
    @Max(99)
    private Integer minSuggAge;

    @NotNull(message = "Minimun play time cannot be blank")
    @Positive(message = "Minimum play time must be a positive integer")
    @Min(1)
    private Integer minPlayTime;

    @NotNull(message = "Maximum play time cannot be blank")
    @Positive(message = "Maximum play time must be a positive integer")
    //@ValidMaxPlayTime //eventualmente fare una validazione a posteriori se questo non funziona
    private Integer maxPlayTime;

    private List<String> designers;
    private List<String> artists;
    private List<String> publisher;

    @NotNull(message = "Categories cannot be null")
    private List<String> categories;

    private List<String> mechanics;
    private List<String> family;
}
