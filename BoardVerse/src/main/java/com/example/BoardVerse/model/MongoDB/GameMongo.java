package com.example.BoardVerse.model.MongoDB;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Document(collection = "games")
@Data
public class GameMongo {

    @Id
    private String id;

    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotNull(message = "Year released cannot be null")
    private Integer yearReleased;
    private Date uploadTime;

    private String description;
    private String shortDescription;

    @Min(value = 0, message = "Average rating must be positive")
    private Double averageRating;
    private int ratingVoters;

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
    private List<String> family;


    @Override
    public boolean equals(Object o) {
        // Verifica se i riferimenti sono uguali
        if (this == o) return true;

        // Verifica se l'oggetto o è null o se sono di classi diverse
        if (o == null || getClass() != o.getClass()) return false;

        // Confronto dei campi effettivi (in questo caso name e yearReleased)
        GameMongo game = (GameMongo) o;
        return yearReleased == game.yearReleased && name.equals(game.name);
    }

    // Implementazione del metodo hashCode
    @Override
    public int hashCode() {
        // Crea un hash code utilizzando i campi importanti per l'uguaglianza (name e yearReleased)
        return Objects.hash(name, yearReleased);
    }

    public GameMongo get() {
        return this;
    }
}
