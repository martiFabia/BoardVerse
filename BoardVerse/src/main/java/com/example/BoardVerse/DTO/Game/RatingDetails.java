package com.example.BoardVerse.DTO.Game;

import lombok.Data;

import java.util.List;

@Data
public class RatingDetails {
    private Double avgRating;
    private Double stdDeviation;
    private List<RatingDistribution> distribution;
}
