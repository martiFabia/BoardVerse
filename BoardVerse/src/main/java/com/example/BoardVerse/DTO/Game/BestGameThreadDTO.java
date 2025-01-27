package com.example.BoardVerse.DTO.Game;import lombok.Data;

@Data
public class BestGameThreadDTO {
    private String id;
    private String name;
    private Integer yearReleased;
    private Double importanceIndex;
}