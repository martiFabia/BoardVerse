package com.example.BoardVerse.model.MongoDB.subentities;

import java.util.Date;

public record ReviewUser(
        String id,
        Date postDate,
        GamePreviewEssential game,
        Double rating,
        String content
) {
}
