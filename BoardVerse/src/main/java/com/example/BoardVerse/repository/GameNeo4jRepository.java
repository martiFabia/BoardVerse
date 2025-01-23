package com.example.BoardVerse.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;


public interface GameNeo4jRepository  extends Neo4jRepository<GameNeo4jRepository, String> {
}
