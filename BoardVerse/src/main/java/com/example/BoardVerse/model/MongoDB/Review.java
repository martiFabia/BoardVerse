package com.example.BoardVerse.model.MongoDB;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document(collection = "reviews")
@Data
public class Review {

    @Id
    private String id;
    private String gameId;
    private String gameName;
    private int GameYearReleased;
    private String authorUsername;
    private Location authorLocation;

    @Min(1) @Max(value = 10, message = "Rating must be between 1 and 10")
    @NotNull
    private Double rating;
    private String comment;

    private Date createdAt;

    // Costruttore vuoto per Spring e MongoDB
    public Review() {
    }

    // Costruttore con parametri
    public Review(String id, String gameId, String authorUsername, Location authorLocation, double rating, String comment, Date createdAt) {
        this.id = id;
        this.gameId = gameId;
        this.authorUsername = authorUsername;
        this.authorLocation = authorLocation;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Review get() {
        return this;
    }

}