package com.example.BoardVerse.DTO.Thread;

import java.util.Date;

public record ThreadPreviewGameDTO(
        String id,
        String tag,
        String authorUsername,
        String content,
        Date postDate,
        Date lastPostDate,
        int messageCount
) {
}
