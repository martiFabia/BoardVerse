package com.example.BoardVerse.DTO.Tournament;

import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.subentities.OptionsTournament;
import com.example.BoardVerse.model.MongoDB.subentities.VisibilityTournament;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateTournamentDTO {
    private String name;

    private String type;
    private String typeDescription;
    private Date startingTime;

    @Schema(description = "Tournament Location (Country, State, City)",
            example = "{ \"country\": \" \", \"stateOrProvince\": \" \", \"city\": \" \" }")
    private Location location;

    @Positive
    private Integer minParticipants;
    @Positive
    private Integer maxParticipants;

    private String winner;
    private VisibilityTournament visibility;
    private List<OptionsTournament> options;
}