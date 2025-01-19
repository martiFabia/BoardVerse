package com.example.BoardVerse.model.MongoDB.subentities;

import lombok.Data;

import java.util.Date;

@Data
public abstract class Post {
    private String authorUsername;
    private Date postDate;
    private String content;
}
