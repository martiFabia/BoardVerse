package com.example.BoardVerse.model.MongoDB.subentities;

import lombok.Data;

@Data
public class GameReview {
    private String id;
    private String name;
    private Integer yearReleased;
    private String shortDescription;

    public GameReview(String id, String name, int yearReleased, String shortDescription) {
        this.id = id;
        this.name = name;
        this.yearReleased = yearReleased;
        this.shortDescription = shortDescription;
    }
}
