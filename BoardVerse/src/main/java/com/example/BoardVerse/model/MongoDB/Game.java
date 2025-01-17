package com.example.BoardVerse.model.MongoDB;

import com.example.BoardVerse.DTO.Review.ReviewGame;
import com.example.BoardVerse.DTO.Review.ReviewInfoDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "games")
@Data
public class Game {

    @Id
    private String id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String description;
    private String shortDescription;
    private String imgURL;

    @Positive(message = "Average rating must be positive")
    private double averageRating;

    @Positive(message = "Number of reviews must be positive")
    private int numberReviews;

    private int yearReleased;
    private int minPlayers;
    private int maxPlayers;
    private int minSuggAge;
    private int minPlayTime;
    private int maxPlayTime;

    private List<String> designers;
    private List<String> artists;
    private List<String> publisher;

    @NotNull(message = "Categories cannot be null")
    private List<String> categories;

    private List<String> mechanics;
    private int numRatings;

    private List<ReviewGame> mostRecentReviews=new ArrayList<>();


    public Game get() {
        return this;
    }

    //GETTER E SETTER

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @NotBlank(message = "Name cannot be blank") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Name cannot be blank") String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    @Positive(message = "Average rating must be positive")
    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(@Positive(message = "Average rating must be positive") double averageRating) {
        this.averageRating = averageRating;
    }

    @Positive(message = "Number of reviews must be positive")
    public int getNumberReviews() {
        return numberReviews;
    }

    public void setNumberReviews(@Positive(message = "Number of reviews must be positive") int numberReviews) {
        this.numberReviews = numberReviews;
    }

    public int getYearReleased() {
        return yearReleased;
    }

    public void setYearReleased(int yearReleased) {
        this.yearReleased = yearReleased;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMinSuggAge() {
        return minSuggAge;
    }

    public void setMinSuggAge(int minSuggAge) {
        this.minSuggAge = minSuggAge;
    }

    public int getMinPlayTime() {
        return minPlayTime;
    }

    public void setMinPlayTime(int minPlayTime) {
        this.minPlayTime = minPlayTime;
    }

    public int getMaxPlayTime() {
        return maxPlayTime;
    }

    public void setMaxPlayTime(int maxPlayTime) {
        this.maxPlayTime = maxPlayTime;
    }

    public List<String> getDesigners() {
        return designers;
    }

    public void setDesigners(List<String> designers) {
        this.designers = designers;
    }

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public List<String> getPublisher() {
        return publisher;
    }

    public void setPublisher(List<String> publisher) {
        this.publisher = publisher;
    }

    public @NotNull(message = "Categories cannot be null") List<String> getCategories() {
        return categories;
    }

    public void setCategories(@NotNull(message = "Categories cannot be null") List<String> categories) {
        this.categories = categories;
    }

    public List<String> getMechanics() {
        return mechanics;
    }

    public void setMechanics(List<String> mechanics) {
        this.mechanics = mechanics;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }
}
