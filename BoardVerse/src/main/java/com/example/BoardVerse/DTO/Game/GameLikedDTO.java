package com.example.BoardVerse.DTO.Game;

import java.time.OffsetDateTime;

public record GameLikedDTO(
        String id,
        String name,
        int yearReleased,
        String shortDescription,
        OffsetDateTime likedAt
) {}
