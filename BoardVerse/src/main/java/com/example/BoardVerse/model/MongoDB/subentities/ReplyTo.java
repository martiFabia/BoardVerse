package com.example.BoardVerse.model.MongoDB.subentities;

import lombok.Data;

@Data
public class ReplyTo {
    private String username;
    private String messageUUID;
    private String contentPreview;

    public ReplyTo(String username, String messageUUID, String contentPreview) {
        this.username = username;
        this.messageUUID = messageUUID;
        this.contentPreview = contentPreview;
    }
}
