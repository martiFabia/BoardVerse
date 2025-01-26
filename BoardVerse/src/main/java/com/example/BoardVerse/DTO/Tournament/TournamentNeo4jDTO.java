package com.example.BoardVerse.DTO.Tournament;

import java.util.Date;

public record TournamentNeo4jDTO(
        String id,
        String name,
        String visibility,
        int maxParticipants,
        Date startingTime
) {

}
