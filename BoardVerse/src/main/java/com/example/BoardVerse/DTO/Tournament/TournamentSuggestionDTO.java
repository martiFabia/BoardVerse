package com.example.BoardVerse.DTO.Tournament;

import lombok.Data;

import java.util.Date;

@Data
public class TournamentSuggestionDTO {
    private String id;
    private String name;
    private int numParticipants;
    private int maxParticipants;
    private String visibility;
    private Date startingTime;
    private String administrator;
}

