package com.example.BoardVerse.DTO.Tournament;

import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.subentities.OptionsTournament;
import com.example.BoardVerse.model.MongoDB.subentities.VisibilityTournament;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AddTournamentDTO {
    @NotBlank
    private String name;

    private String type;
    private String typeDescription;

    @Future
    private Date startingTime;

    @Schema(description = "TournamentMongo Location (Country, State, City)",
            example = "{ \"country\": \" \", \"stateOrProvince\": \" \", \"city\": \" \" }")
    private Location location;

    @Positive
    private Integer minParticipants;
    @Positive
    private Integer maxParticipants;

    private VisibilityTournament visibility;

    private List<OptionsTournament> options=new ArrayList<>();
    private List<String> allowed;
}