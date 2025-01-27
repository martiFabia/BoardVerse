package com.example.BoardVerse.DTO.Tournament;

import java.time.OffsetDateTime;
import java.util.List;

public record ExtendedTournamentNeo4jInfo (

        String id,
        String name,
        OffsetDateTime startingTime,
        int maxParticipants,
        String visibility,
        List<String> participants

){

}
