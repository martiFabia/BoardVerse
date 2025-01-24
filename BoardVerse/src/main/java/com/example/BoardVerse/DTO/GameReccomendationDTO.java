package com.example.BoardVerse.DTO;

public record GameReccomendationDTO (
        String id,
        String name,
        Integer yearReleased,
        String shortDescription,
        Double similarity
){}