package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.GameAnalyticsDTO;
import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import lombok.NonNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;


public interface GameNeo4jRepository  extends Neo4jRepository<GameNeo4j, String> {

    /**
     *  Finds a game by its name and year
     *
     *  @param name the name of the game
     *  @param yearReleased the year the game was released
     *  @return the game with the given name
     */
    Optional<GameNeo4j> findGameNeo4jByNameAndYearReleased(String name, int yearReleased);

    /**
     *  Removes a game and all related tournaments
     *
     *  @param gameId the ID of the game to be removed
     */
    @Query("MATCH (g:Game {_id: $gameId})<-[:IS_RELATED_TO]-(t:Tournament)" +
            "DETACH DELETE g, t")
    void deleteById(@NonNull String gameId);


    /*====================================== ANALYTICS ======================================*/

    /**
     *  Returns the number of users that like a game and the number of tournaments related to it
     *
     *  @param gameId the ID of the game
     *  @return a list of GameAnalyticsDTO objects
     */
    @Query("""
            MATCH (game:Game {_id: $gameId})<-[:LIKES]-(user:User)
            WITH game, COUNT(user) AS likeCount
            MATCH (game)<-[:IS_RELATED_TO]-(tournament:Tournament)
            WITH game, likeCount,
                 COUNT(tournament) AS totalTournaments,
                 COUNT(CASE WHEN EXISTS { MATCH (tournament)<-[:WINNER]-(:User) } THEN tournament END) AS finishedTournaments,
                 COUNT(CASE WHEN tournament.startingTime <= datetime() THEN tournament END) AS ongoingTournaments,
                 COUNT(CASE WHEN tournament.startingTime > datetime()
                  AND NOT EXISTS { MATCH (tournament)<-[:WINNER]-(:User) }
                  THEN tournament END) AS futureTournaments,
                 COUNT(CASE WHEN tournament.visibility = 'PUBLIC' THEN tournament END) AS publicTournaments,
                 COUNT(CASE WHEN tournament.visibility = 'PRIVATE' THEN tournament END) AS privateTournaments,
                 COUNT(CASE WHEN tournament.visibility = 'INVITE' THEN tournament END) AS inviteTournaments
            RETURN
              game._id AS gameId,
              game.name AS gameName,
              likeCount,
              totalTournaments,
              finishedTournaments,
              ongoingTournaments,
              futureTournaments,
              publicTournaments,
              privateTournaments,
              inviteTournaments
    """)
    List<GameAnalyticsDTO> getGameAnalytics(String gameId);
}



