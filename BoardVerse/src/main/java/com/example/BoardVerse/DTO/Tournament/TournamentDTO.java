package com.example.BoardVerse.DTO.Tournament;

import com.example.BoardVerse.model.MongoDB.subentities.GamePreviewEssential;
import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.subentities.OptionsTournament;
import com.example.BoardVerse.model.MongoDB.subentities.VisibilityTournament;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TournamentDTO {

    private String name;
    private GamePreviewEssential game;

    private String type;
    private String typeDescription;
    private Date startingTime;

    private Location location;

    private Integer numParticipants;
    private Integer minParticipants;
    private Integer maxParticipants;

    private String administrator;
    private String winner;

    private VisibilityTournament visibility;

    private List<OptionsTournament> options = new ArrayList<>();

    public TournamentDTO(
            String name,
            GamePreviewEssential game,
            String type,
            String typeDescription,
            Date startingTime,
            Location location,
            Integer numParticipants,
            Integer minParticipants,
            Integer maxParticipants,
            String administrator,
            String winner,
            VisibilityTournament visibility,
            List<OptionsTournament> options
    ) {
        this.name = name;
        this.game = game;
        this.type = type;
        this.typeDescription = typeDescription;
        this.startingTime = startingTime;
        this.location = location;
        this.numParticipants = numParticipants;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.administrator = administrator;
        this.winner = winner;
        this.visibility = visibility;
        this.options.addAll(options);
    }
}
