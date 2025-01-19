package com.example.BoardVerse.model.MongoDB;

import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.subentities.AuthorReview;
import com.example.BoardVerse.model.MongoDB.subentities.GameReview;
import com.example.BoardVerse.model.MongoDB.subentities.Post;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "reviews")
@Data
public class Review extends Post {

    @Id
    private String id;
    //private Date posDate;

    private Date authorBirthDate;
    private Location location;
    private GameReview game;

    @Min(1) @Max(value = 10, message = "Rating must be between 1 and 10")
    @NotNull
    private Double rating;
    //private String content;



    // Costruttore vuoto per Spring e MongoDB
    public Review() {
    }

    /*
    // Costruttore con parametri
    public Review(String id, String gameId, String authorUsername, Location authorLocation, double rating, String comment, Date reviewDate) {
        this.id = id;
        this.gameId = gameId;
        this.authorUsername = authorUsername;
        this.authorLocation = authorLocation;
        this.rating = rating;
        this.content = comment;
        this.reviewDate = reviewDate;
    }

     */

    public Review get() {
        return this;
    }



}