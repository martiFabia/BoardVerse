package com.example.BoardVerse.model.MongoDB.subentities;

import lombok.Data;

@Data
public class Message extends Post{
    private String id;
    private ReplyTo replyTo;
}
