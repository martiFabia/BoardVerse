package com.example.BoardVerse.model.Neo4j;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.OffsetDateTime;
import java.util.Date;


@Node("Tournament")
@Data
public class TournamentNeo4j {
    @Id
    @Property("_id")
    private String id;

    @Property("name")
    @NotBlank
    private String name;

    @Property("visibility")
    @NotBlank
    private String visibility;

    @Property("maxParticipants")
    @NotNull
    @Positive
    private int maxParticipants;

    @Property("startingTime")
    @NotNull
    private OffsetDateTime startingTime;

}
