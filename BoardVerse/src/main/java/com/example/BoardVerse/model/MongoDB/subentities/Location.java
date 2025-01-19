package com.example.BoardVerse.model.MongoDB.subentities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "User Location (Country, State, City)")
@Data
public class Location {
    @Schema(description = "Country", example = "Italy")
    private String country;

    @Schema(description = "State", example = "Tuscany")
    private String state;

    @Schema(description = "City", example = "Pisa")
    private String city;

    public Location(){

    }

    // Costruttore con parametri
    public Location(String country, String state, String city) {
        this.country = country;
        this.state = state;
        this.city = city;
    }
}