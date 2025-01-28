package com.example.BoardVerse.DTO.Thread;

import java.util.Date;

public record ThreadPreviewGameDTO(
        String id,
        String tag,
        Date lastPostDate,
        String authorUsername,
        Date postDate,
        String content,
        Integer messageCount
) {
}
