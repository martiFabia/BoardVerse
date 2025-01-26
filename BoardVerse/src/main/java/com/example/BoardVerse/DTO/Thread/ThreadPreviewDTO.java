package com.example.BoardVerse.DTO.Thread;

import com.example.BoardVerse.model.MongoDB.subentities.GamePreviewEssential;

import java.util.Date;

public record ThreadPreviewDTO (
    String id,
    String tag,
    GamePreviewEssential game,
    String authorUsername,
    String content,
    Date postDate,
    Date lastPostDate,
    int messageCount
    ) {}
