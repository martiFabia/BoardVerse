package com.example.BoardVerse.DTO.Tournament;

import java.util.List;

public record ExtendedTournamentNeo4jInfo (

        String id,
        String name,
        String startingTime,
        int maxParticipants,
        String visibility,
        List<String> participants

){

}
