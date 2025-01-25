package com.example.BoardVerse.DTO.Review;

import java.util.Date;

public record ReviewUserDTO(
        String id,
        String game,
        Integer yearReleased,
        Double rating,
        String content,
        Date postDate
) {
}
