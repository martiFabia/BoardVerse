package com.example.BoardVerse.DTO.Review;

import java.util.Date;

public record ReviewGame(
        String id,
        String authorUsername,
        Double rating,
        String comment,
        Date createdAt
) {
}
