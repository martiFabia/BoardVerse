package com.example.BoardVerse.model.Neo4j;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("UserMongo")
@Data
public class UserNeo4j {
    @Id
    @Property("username")
    private String username;
}
