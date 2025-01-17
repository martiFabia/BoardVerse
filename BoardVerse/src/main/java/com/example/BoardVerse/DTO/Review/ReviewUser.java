package com.example.BoardVerse.DTO.Review;

import java.util.Date;

public record ReviewUser(
        String id,
        String gameId,
        Double rating,
        String comment,
        Date createdAt
) {
}
