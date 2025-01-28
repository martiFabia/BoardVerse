package com.example.BoardVerse.DTO.Tournament;

import com.example.BoardVerse.DTO.User.activity.GameDTO;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Date;

public record TournamentSuggestionDTO(
        String id,
        String name,
        OffsetDateTime startingTime,
        Integer numParticipants,
        Integer maxParticipants,
        Integer numParticipantFollowers,
        GameDTO game
) {}
