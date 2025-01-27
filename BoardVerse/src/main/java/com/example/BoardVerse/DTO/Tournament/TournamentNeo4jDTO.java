package com.example.BoardVerse.DTO.Tournament;

import com.example.BoardVerse.DTO.Game.GamePreviewEssentialDTO;

import java.time.OffsetDateTime;

public record TournamentNeo4jDTO(
        String id,
        String name,
        String visibility,
        int maxParticipants,
        OffsetDateTime startingTime,
        GamePreviewEssentialDTO game
) {}