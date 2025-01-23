package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;


public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4jRepository, String> {

    // Aggiungere un gioco ai preferiti di un utente
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {gameId: $gameId}) " +
            "MERGE (u)-[:LIKES {timestamp: datetime()}]->(g)")
    void addLikeToGame(String username, String gameId);

    // Rimuovere un gioco dai preferiti di un utente
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {gameId: $gameId}) " +
            "MATCH (u)-[r:LIKES]->(g) " +
            "DELETE r")
    void removeLikeFromGame(String username, String gameId);

    // Seguire un utente
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (u2:User {username: $usernameToFollow}) " +
            "MERGE (u)-[:FOLLOWS {timestamp: datetime()}]->(u2)")
    void followUser(String username, String usernameToFollow);

    // Smettere di seguire un utente
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (u2:User {username: $usernameToUnfollow}) " +
            "MATCH (u)-[r:FOLLOWS]->(u2) " +
            "DELETE r")
    void unfollowUser(String username, String usernameToUnfollow);

    @Query("""
        MATCH (u:User {username: $username}) 
        MATCH (g:Game {id: $gameId})
        CREATE (t:Tournament {
            id: $tournamentId,
            name: $name,
            visibility: $visibility,
            maxParticipants: $maxParticipants,
            startingTime: datetime($startingTime)
        }) 
        CREATE (u)-[:CREATES {timestamp: datetime()}]->(t)
        CREATE (t)-[:RELATED_TO]->(g)
        """)
    void createTournament(String username, String tournamentId, String name, String visibility, int maxParticipants, String startingTime, String gameId);

    // partecipare a un torneo
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "MERGE (u)-[:PARTICIPATES {timestamp: datetime()}]->(t)")
    void participateToTournament(String username, String tournamentId);

    // vincere un torneo
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "MERGE (u)-[:WON {timestamp: datetime()}]->(t)")
    void winTournament(String username, String tournamentId);

    //Trovare tutti i giochi preferiti di un utente
    @Query("MATCH (u:User {username: $username})-[:LIKES]->(g:Game) " +
            "RETURN g")
    List<GameNeo4j> findLikedGames(String username);

    //Trovare tutti gli utenti seguiti da un utente
    @Query("MATCH (u:User {username: $username})-[:FOLLOWS]->(u2:User) " +
            "RETURN u2")
    List<UserNeo4j> findFollowedUsers(String username);

    //Trovare tutti gli utenti che seguono un utente
    @Query("MATCH (u:User {username: $username})<-[:FOLLOWS]-(u2:User) " +
            "RETURN u2")
    List<UserNeo4j> findFollowers(String username);

    //Trovare tutti i tornei creati da un utente
    @Query("MATCH (u:User {username: $username})-[:CREATES]->(t:Tournament) " +
            "RETURN t")
    List<TournamentNeo4j> findCreatedTournaments(String username);

    //Trovare tutti i tornei a cui partecipa un utente
    @Query("MATCH (u:User {username: $username})-[:PARTICIPATES]->(t:Tournament) " +
            "RETURN t")
    List<TournamentNeo4j> findParticipatedTournaments(String username);

    //Trovare tutti i tornei vinti da un utente
    @Query("MATCH (u:User {username: $username})-[:WON]->(t:Tournament) " +
            "RETURN t")
    List<TournamentNeo4j> findWonTournaments(String username);
}


