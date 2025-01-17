package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.DTO.Review.ReviewUser;
import com.example.BoardVerse.model.MongoDB.Review;

public class ReviewMapper {

    public static ReviewUser toUser(Review review) {
        return new ReviewUser(review.getId(), review.getGameId(), review.getRating(), review.getComment(), review.getCreatedAt());
    }

    public static ReviewInfo toGame(Review review) {
        return new ReviewInfo(review.getId(), review.getAuthorUsername(), review.getRating(), review.getComment(), review.getCreatedAt());
    }

}
