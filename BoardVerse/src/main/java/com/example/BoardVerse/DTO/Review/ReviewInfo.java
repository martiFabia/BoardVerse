package com.example.BoardVerse.DTO.Review;

import java.util.Date;

public record ReviewInfo(
        String id,
        String authorUsername,
        Double rating,
        String comment,
        Date createdAt
) {
}
