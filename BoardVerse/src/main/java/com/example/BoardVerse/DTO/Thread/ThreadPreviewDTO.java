package com.example.BoardVerse.DTO.Thread;

import com.example.BoardVerse.model.MongoDB.subentities.GameThread;

import java.util.Date;

public record ThreadPreviewDTO (
    String id,
    String tag,
    GameThread game,
    String authorUsername,
    String content,
    Date postDate,
    Date lastPostDate,
    int messageCount
    ) {}
