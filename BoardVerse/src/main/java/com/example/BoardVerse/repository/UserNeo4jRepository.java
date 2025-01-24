package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

/**
 * Repository interface for UserNeo4j entities.
 * Provides methods to perform various actions and queries related to users, games, and tournaments.
 */
public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4jRepository, String> {

    /**
     * Adds a user to the database.
     *
     * @param username the username of the user
     */
    @Query("CREATE (u:User {username: $username})")
    void addUser(String username);

    /**
     * Removes a user from the database.
     *
     * @param username the username of the user
     */
    @Query("MATCH (u:User {username: $username}) " +
            "DETACH DELETE u")
    void removeUser(String username);

    /**
     * Updates the username of a user.
     *
     * @param oldUsername the old username of the user
     * @param newUsername the new username of the user
     */
    @Query("MATCH (u:User {username: $oldUsername}) " +
            "SET u.username = $newUsername")
    void updateUsername(String oldUsername, String newUsername);


    /*============================ USER-GAME ACTIONS ==============================*/

    /**
     * Adds a game to the user's favorites.
     *
     * @param username the username of the user
     * @param gameId the ID of the game to be added to favorites
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {gameId: $gameId}) " +
            "MERGE (u)-[:LIKES {timestamp: datetime()}]->(g)")
    void addLikeToGame(String username, String gameId);

    /**
     * Removes a game from the user's favorites.
     *
     * @param username the username of the user
     * @param gameId the ID of the game to be removed from favorites
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {gameId: $gameId}) " +
            "MATCH (u)-[r:LIKES]->(g) " +
            "DELETE r")
    void removeLikeFromGame(String username, String gameId);


    /*============================ USER-USER ACTIONS ==============================*/

    /**
     * Follows another user.
     *
     * @param username the username of the user who wants to follow
     * @param usernameToFollow the username of the user to be followed
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (u2:User {username: $usernameToFollow}) " +
            "MERGE (u)-[:FOLLOWS {timestamp: datetime()}]->(u2)")
    void followUser(String username, String usernameToFollow);

    /**
     * Unfollows another user.
     *
     * @param username the username of the user who wants to unfollow
     * @param usernameToUnfollow the username of the user to be unfollowed
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (u2:User {username: $usernameToUnfollow}) " +
            "MATCH (u)-[r:FOLLOWS]->(u2) " +
            "DELETE r")
    void unfollowUser(String username, String usernameToUnfollow);


    /*============================ USER-TOURNAMENT ACTIONS ==============================*/

    /**
     * Creates a tournament.
     *
     * @param username the username of the user creating the tournament
     * @param tournamentId the ID of the tournament
     * @param name the name of the tournament
     * @param visibility the visibility of the tournament
     * @param maxParticipants the maximum number of participants in the tournament
     * @param startingTime the starting time of the tournament
     * @param gameId the ID of the game related to the tournament
     */
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

    /**
     * Participates in a tournament.
     *
     * @param username the username of the user participating in the tournament
     * @param tournamentId the ID of the tournament
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "MERGE (u)-[:PARTICIPATES {timestamp: datetime()}]->(t)")
    void participateToTournament(String username, String tournamentId);

    /**
     * Removes a user from a tournament.
     *
     * @param username the username of the user to be removed from the tournament
     * @param tournamentId the ID of the tournament
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "MATCH (u)-[r:PARTICIPATES]->(t) " +
            "DELETE r")
    void removeUserFromTournament(String username, String tournamentId);

    /**
     * Wins a tournament.
     *
     * @param username the username of the user winning the tournament
     * @param tournamentId the ID of the tournament
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "MERGE (u)-[:WON {timestamp: datetime()}]->(t)")
    void winTournament(String username, String tournamentId);

    /**
     * Updates  a tournament.
     *
     * @param tournamentId the ID of the tournament
     * @param newName the new name of the tournament
     * @param newVisibility the new visibility of the tournament
     * @param newMaxParticipants the new maximum number of participants in the tournament
     * @param newStartingTime the new starting time of the tournament
     */
    @Query("""
        MATCH (t:Tournament {id: $tournamentId})
        SET t.name = $newName,
            t.visibility = $newVisibility,
            t.maxParticipants = $newMaxParticipants,
            t.startingTime = datetime($newStartingTime)
        """)
    void updateTournament(String tournamentId, String newName, String newVisibility, int newMaxParticipants, String newStartingTime);

    /**
     * Removes a tournament.
     *
     * @param tournamentId the ID of the tournament to be removed
     */
    @Query("MATCH (t:Tournament {id: $tournamentId}) " +
            "DETACH DELETE t")
    void removeTournament(String tournamentId);


    /*============================ FINDS ==============================*/

    /**
     * Finds all games liked by a user.
     *
     * @param username the username of the user
     * @return a list of games liked by the user
     */
    @Query("MATCH (u:User {username: $username})-[:LIKES]->(g:Game) " +
            "RETURN g")
    List<GameNeo4j> findLikedGames(String username);

    /**
     * Finds all users followed by a user.
     *
     * @param username the username of the user
     * @return a list of users followed by the user
     */
    @Query("MATCH (u:User {username: $username})-[:FOLLOWS]->(u2:User) " +
            "RETURN u2")
    List<UserNeo4j> findFollowedUsers(String username);

    /**
     * Finds all users following a user.
     *
     * @param username the username of the user
     * @return a list of users following the user
     */
    @Query("MATCH (u:User {username: $username})<-[:FOLLOWS]-(u2:User) " +
            "RETURN u2")
    List<UserNeo4j> findFollowers(String username);

    /**
     * Finds all tournaments created by a user.
     *
     * @param username the username of the user
     * @return a list of tournaments created by the user
     */
    @Query("MATCH (u:User {username: $username})-[:CREATED]->(t:Tournament) " +
            "RETURN t")
    List<TournamentNeo4j> findCreatedTournaments(String username);

    /**
     * Finds all tournaments a user participates in.
     *
     * @param username the username of the user
     * @return a list of tournaments the user participates in
     */
    @Query("MATCH (u:User {username: $username})-[:PARTICIPATES]->(t:Tournament) " +
            "RETURN t")
    List<TournamentNeo4j> findParticipatedTournaments(String username);

    /**
     * Finds all tournaments won by a user.
     *
     * @param username the username of the user
     * @return a list of tournaments won by the user
     */
    @Query("MATCH (u:User {username: $username})-[:WON]->(t:Tournament) " +
            "RETURN t")
    List<TournamentNeo4j> findWonTournaments(String username);


    /*============================ SUGGESTIONS ==============================*/

    /**
     * TODO Suggests games to a user based on the games they like.
     *
     * @param username the username of the user
     * @return a list of games suggested to the user
     */
    @Query("MATCH (u:User {username: $username})-[:LIKES]->(g:Game)<-[:LIKES]-(u2:User)-[:LIKES]->(g2:Game) " +
            "WHERE NOT (u)-[:LIKES]->(g2) " +
            "RETURN g2")
    List<GameNeo4j> suggestGames(String username);

    /**
     * TODO Suggests users to a user based on the users they follow.
     *
     * @param username the username of the user
     * @return a list of users suggested to the user
     */
    @Query("MATCH (u:User {username: $username})-[:FOLLOWS]->(u2:User)-[:FOLLOWS]->(u3:User) " +
            "WHERE NOT (u)-[:FOLLOWS]->(u3) " +
            "RETURN u3")
    List<UserNeo4j> suggestUsers(String username);

    /**
     * TODO Suggests tournaments to a user based on the tournaments they participate in.
     *
     * @param username the username of the user
     * @return a list of tournaments suggested to the user
     */
    @Query("MATCH (u:User {username: $username})-[:PARTICIPATES]->(t:Tournament)<-[:PARTICIPATES]-(u2:User)-[:PARTICIPATES]->(t2:Tournament) " +
            "WHERE NOT (u)-[:PARTICIPATES]->(t2) " +
            "RETURN t2")
    List<TournamentNeo4j> suggestTournaments(String username);


    /*============================ ANALYTICS ==============================*/


}