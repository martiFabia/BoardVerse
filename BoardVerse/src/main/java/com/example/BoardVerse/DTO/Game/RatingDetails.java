package com.example.BoardVerse.DTO.Game;

import lombok.Data;

import java.util.List;

@Data
public class RatingDetails {
    private List<RatingDistribution> distribution;
    private Double stdDevRating;
}
