package com.example.BoardVerse.model.MongoDB.subentities;

import lombok.Data;

@Data
public class GameReview {
    private String id;
    private String name;
    private int yearReleased;
    private String shortDescription;
}
