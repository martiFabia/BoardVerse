package com.example.BoardVerse.DTO.Game;

public record GameRankPreviewDTO(
        String id,
        String name,
        Integer yearReleased,
        String shortDescription,
        Double averageRating
) {
}
