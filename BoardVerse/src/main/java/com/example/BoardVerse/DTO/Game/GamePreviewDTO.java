package com.example.BoardVerse.DTO.Game;

public record GamePreviewDTO(
        String id,
        String name,
        Integer yearReleased,
        String shortDescription,
        Double averageRating
) {
}
