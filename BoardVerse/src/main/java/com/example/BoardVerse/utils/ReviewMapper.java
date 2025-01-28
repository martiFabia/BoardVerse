package com.example.BoardVerse.utils;

import com.example.BoardVerse.model.MongoDB.subentities.GamePreviewEssential;
import com.example.BoardVerse.model.MongoDB.subentities.GameReview;
import com.example.BoardVerse.model.MongoDB.subentities.ReviewUser;
import com.example.BoardVerse.model.MongoDB.ReviewMongo;

public class ReviewMapper {
    public static GamePreviewEssential fromGame(GameReview game) {
        return new GamePreviewEssential(game.getId(), game.getName(), game.getYearReleased());
    }

    public static ReviewUser toUser(ReviewMongo reviewMongo) {
        GamePreviewEssential gamePreview = ReviewMapper.fromGame(reviewMongo.getGame());

        return new ReviewUser(
                reviewMongo.getId(),
                reviewMongo.getPostDate(),
                gamePreview,
                reviewMongo.getRating(),
                reviewMongo.getContent()
        );
    }




}
