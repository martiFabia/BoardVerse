package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.model.MongoDB.subentities.ReviewUser;
import com.example.BoardVerse.model.MongoDB.Review;

public class ReviewMapper {

    public static ReviewUser toUser(Review review) {
        return new ReviewUser(review.getId(),review.getPostDate(), review.getGame(), review.getRating(), review.getContent() );
    }

    public static ReviewInfo toInfo(Review review) {
        return new ReviewInfo(review.getId(), review.getAuthorUsername(), review.getRating(), review.getContent(), review.getPostDate());
    }

}
