package com.example.BoardVerse.model.MongoDB.subentities;

import lombok.Data;

@Data
public class GamePreviewEssential {
    private String id;
    private String name;
    private int yearReleased;


    public GamePreviewEssential(String id, String name, int yearReleased) {
        this.id = id;
        this.name = name;
        this.yearReleased = yearReleased;
    }
}
