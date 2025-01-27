package com.example.BoardVerse.DTO.User.activity;

public record ActivityTournamentDTO (
    String id,
    String name,
    GameDTO game
) implements ActivityPropertiesDTO {
}
