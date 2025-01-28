package com.example.BoardVerse.DTO.Game;

public record GameSuggestionDTO(
        String id,
        String name,
        Integer yearReleased,
        String shortDescription,
        Integer commonCategories
){}