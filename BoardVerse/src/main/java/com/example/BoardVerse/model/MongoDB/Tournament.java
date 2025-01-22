package com.example.BoardVerse.model.MongoDB;


import com.example.BoardVerse.model.MongoDB.subentities.GameThread;
import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.subentities.OptionsTournament;
import com.example.BoardVerse.model.MongoDB.subentities.VisibilityTournament;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "tournaments")
public class Tournament {
    @Id
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private GameThread game;

    private String type;
    private String typeDescription;
    private Date startingTime;

    private Location location;

    @Positive
    private Integer numParticipants;
    @Positive
    private Integer minParticipants;
    @Positive
    private Integer maxParticipants;

    private String administrator;
    private String winner;

    private VisibilityTournament visibility;

    private OptionsTournament options;
    private List<String> allowed;

}