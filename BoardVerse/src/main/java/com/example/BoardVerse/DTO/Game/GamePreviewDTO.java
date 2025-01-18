package com.example.BoardVerse.DTO.Game;

public record GamePreviewDTO(
        String id,
        String name,
        String ShortDescription,
        int yearReleased,
        Double averageRating
) {
}
