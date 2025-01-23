package com.example.BoardVerse.model.Neo4j;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("Game")
@Data
public class GameNeo4j {
    @Id
    private String id;

    @Property("name")
    @NotBlank
    private String name;

    @Property("yearReleased")
    @NotNull
    @Positive
    private int yearReleased;

    @Property("shortDescription")
    @NotBlank
    private String shortDescription;

    @Property("categories")
    @NotEmpty
    private List<String> categories;
}
