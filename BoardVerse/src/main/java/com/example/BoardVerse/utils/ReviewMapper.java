package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Review.ReviewGame;
import com.example.BoardVerse.DTO.Review.ReviewInfoDTO;
import com.example.BoardVerse.DTO.Review.ReviewUser;
import com.example.BoardVerse.model.MongoDB.Review;

public class ReviewMapper {

    public static ReviewInfoDTO toInfoDTO(Review review) {
        return new ReviewInfoDTO(review.getAuthorUsername(), review.getRating(), review.getComment(), review.getCreatedAt());
    }

    public static ReviewUser toUser(Review review) {
        return new ReviewUser(review.getId(), review.getGameId(), review.getRating(), review.getComment(), review.getCreatedAt());
    }

    public static ReviewGame toGame(Review review) {
        return new ReviewGame(review.getId(), review.getAuthorUsername(), review.getRating(), review.getComment(), review.getCreatedAt());
    }

}
