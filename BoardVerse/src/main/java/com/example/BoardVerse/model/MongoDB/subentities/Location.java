package com.example.BoardVerse.model.MongoDB.subentities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "User Location (Country, State, City)")
@Data
public class Location {
    @Schema(description = "Country", example = " ")
    private String country;

    @Schema(description = "State", example = " ")
    private String stateOrProvince;

    @Schema(description = "City", example = " ")
    private String city;

    public Location(){

    }

    // Costruttore con parametri
    public Location(String country, String state, String city) {
        this.country = country;
        this.stateOrProvince = state;
        this.city = city;
    }
}