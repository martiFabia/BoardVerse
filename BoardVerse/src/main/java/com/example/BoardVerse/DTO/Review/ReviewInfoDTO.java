package com.example.BoardVerse.DTO.Review;

import java.util.Date;


public record ReviewInfoDTO(
       // String gameId,
        String authorUsername,
        Double rating,
        String comment,
        Date createdAt
) {
}
