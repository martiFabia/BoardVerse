package com.example.BoardVerse.DTO.Game;

import lombok.Data;

@Data
public class MostPlayedGameDTO {
    private String gameId;
    private String name;
    private Integer yearReleased;
    private double averageWeightedParticipants;
}