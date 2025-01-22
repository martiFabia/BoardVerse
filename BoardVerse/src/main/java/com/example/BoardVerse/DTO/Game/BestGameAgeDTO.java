package com.example.BoardVerse.DTO.Game;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class BestGameAgeDTO {
    // "_id" corrisponde alla fascia di età (es. "10-19")
    @Field("_id")
    private String ageBracket;
    private String game;
    private String name;
    private Integer yearReleased;
    private Double bestAvgRating;

}
