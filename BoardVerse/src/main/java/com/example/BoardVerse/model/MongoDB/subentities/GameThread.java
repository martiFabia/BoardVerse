package com.example.BoardVerse.model.MongoDB.subentities;

import lombok.Data;

@Data
public class GameThread {
    private String id;
    private String name;
    private int yearReleased;


    public GameThread(String id, String name, int yearReleased) {
        this.id = id;
        this.name = name;
        this.yearReleased = yearReleased;
    }
}
