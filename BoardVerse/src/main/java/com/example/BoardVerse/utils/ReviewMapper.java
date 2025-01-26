package com.example.BoardVerse.utils;

import com.example.BoardVerse.model.MongoDB.subentities.ReviewUser;
import com.example.BoardVerse.model.MongoDB.ReviewMongo;

public class ReviewMapper {

    public static ReviewUser toUser(ReviewMongo reviewMongo) {
        return new ReviewUser(reviewMongo.getId(), reviewMongo.getPostDate(), reviewMongo.getGame(), reviewMongo.getRating(), reviewMongo.getContent() );
    }

}
