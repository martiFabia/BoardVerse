package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;


public interface GameNeo4jRepository  extends Neo4jRepository<GameNeo4j, String> {

    /**
     *  Removes a game and all related tournaments
     *
     *  @param gameId the ID of the game to be removed
     */
    @Query("MATCH (g:Game {gameId: $gameId})<-[:IS_RELATED_TO]-(t:Tournament)" +
            "DETACH DELETE g, t")
    void removeGame(String gameId);

    /**
     *  Updates the name of a game
     *
     *  @param gameId the ID of the game to be updated
     *  @param newName the new name of the game
     *  @param newYearReleased the new year the game was released
     *  @param newShortDescription the new description of the game
     *  @param newCategories the new categories of the game
     */
    @Query("MATCH (g:Game {gameId: $gameId})" +
            "SET g.name = $newName, " +
            "   g.yearReleased = $newYearReleased, " +
            "   g.shortDescription = $newShortDescription, " +
            "   g.categories = $newCategories"
    )
    void updateGame(String gameId, String newName, int newYearReleased, String newShortDescription, List<String> newCategories);

}



