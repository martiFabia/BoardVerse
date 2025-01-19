package com.example.BoardVerse.DTO.Review;

import com.example.BoardVerse.model.MongoDB.subentities.GameReview;

import java.util.Date;

public record ReviewUser(
        String id,
        Date reviewDate,
        GameReview game,
        Double rating,
        String comment
) {
}
