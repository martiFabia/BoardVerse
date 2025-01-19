package com.example.BoardVerse.model.MongoDB.subentities;

import java.util.Date;

public record ReviewUser(
        String id,
        Date reviewDate,
        GameReview game,
        Double rating,
        String comment
) {
}
