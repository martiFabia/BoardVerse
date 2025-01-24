package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.FollowersActivityDTO;
import com.example.BoardVerse.DTO.GameReccomendationDTO;
import com.example.BoardVerse.DTO.PersonalActivityDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentSuggestionDTO;
import com.example.BoardVerse.DTO.TournamentNeo4jDTO;
import com.example.BoardVerse.DTO.User.UserSimilarityDTO;
import com.example.BoardVerse.DTO.User.UserSuggestionDTO;
import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserNeo4j entities.
 * Provides methods to perform various actions and queries related to users, games, and tournaments.
 */
public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4j, String> {

    Optional<UserNeo4j> findByUsername(String username);

    /**
     * Removes a user from the database.
     *
     * @param username the username of the user
     * @return true if the user was removed, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "DETACH DELETE u" +
            "RETURN count(u) > 0")
    boolean removeUser(String username);


    /*============================ USER-GAME ACTIONS ==============================*/

    /**
     * Adds a game to the user's favorites.
     *
     * @param username the username of the user
     * @param gameId the ID of the game to be added to favorites
     * @return true if the game was added to favorites, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {gameId: $gameId}) " +
            "MERGE (u)-[l:LIKES]->(g) " +
            "ON CREATE SET l.timestamp = datetime() " +
            "RETURN count(g) > 0")
    boolean addLikeToGame(String username, String gameId);

    /**
     * Removes a game from the user's favorites.
     *
     * @param username the username of the user
     * @param gameId the ID of the game to be removed from favorites
     * @return true if the game was removed from favorites, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {gameId: $gameId}) " +
            "MATCH (u)-[r:LIKES]->(g) " +
            "DELETE r" +
            "RETURN count(g) > 0")
    boolean removeLikeFromGame(String username, String gameId);


    /*============================ USER-USER ACTIONS ==============================*/

    /**
     * Follows another user.
     *
     * @param followerId the ID of the user who wants to follow
     * @param followedId the ID of the user to be followed
     * @return true if the follow operation was successful, false otherwise
     */
    @Query("MATCH (follower:User {username: $followerId}), MATCH (followed:User {username: $followedId}) " +
            "MERGE (follower)-[f:FOLLOWS]->(followed) " +
            "ON CREATE SET f.since = datetime() " +
            "RETURN count(follower) > 0")
    boolean followUser(String followerId, String followedId);

    /**
     * Unfollows another user.
     *
     * @param username the username of the user who wants to unfollow
     * @param usernameToUnfollow the username of the user to be unfollowed
     * @return true if the unfollow operation was successful, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (u2:User {username: $usernameToUnfollow}) " +
            "MATCH (u)-[r:FOLLOWS]->(u2) " +
            "DELETE r " +
            "RETURN count(r) > 0")
    boolean unfollowUser(String username, String usernameToUnfollow);


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
     * @return true if the tournament was created, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {gameId: $gameId}) " +
            "MERGE (t:Tournament {tournamentId: $tournamentId}) " +
            "ON CREATE SET t.name = $name, " +
            "              t.visibility = $visibility, " +
            "              t.maxParticipants = $maxParticipants, " +
            "              t.startingTime = datetime($startingTime) " +
            "MERGE (u)-[:CREATED {timestamp: datetime()}]->(t) " +
            "MERGE (t)-[:IS_RELATED_TO]->(g) " +
            "RETURN count(t) > 0"
    )
    boolean createTournament(String username, String tournamentId, String name, String visibility, int maxParticipants, String startingTime, String gameId);

    /**
     * Participates in a tournament.
     *
     * @param username the username of the user participating in the tournament
     * @param tournamentId the ID of the tournament
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "WHERE size((t)<-[:PARTICIPATES]-()) < t.maxParticipants " +
            "   AND t.startingTime > datetime()" +
            "MERGE (u)-[:PARTICIPATES {timestamp: datetime()}]->(t) " +
            "RETURN true")
    boolean participateToTournament(String username, String tournamentId);

    /**
     * Removes a user from a tournament.
     *
     * @param username the username of the user to be removed from the tournament
     * @param tournamentId the ID of the tournament
     * @return true if the user was removed from the tournament, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "MATCH (u)-[r:PARTICIPATES]->(t) " +
            "DELETE r" +
            "RETURN count(r) > 0"
    )
    boolean removeUserFromTournament(String username, String tournamentId);

    /**
     * Wins a tournament.
     *
     * @param username the username of the user winning the tournament
     * @param tournamentId the ID of the tournament
     * @return true if the user won the tournament, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {tournamentId: $tournamentId}) " +
            "MERGE (u)-[:WON {timestamp: datetime()}]->(t)" +
            "RETURN count(t) > 0")
    boolean winTournament(String username, String tournamentId);


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
    @Query("MATCH (u:User {username: $username})-[:CREATED]->(t:Tournament)-[:IS_RELATED_TO]->(g:Game) " +
            "RETURN " +
            "   CASE " +
            "       WHEN t.visibility <> 'PUBLIC' AND  $username <> $currentUsername" +
            "       THEN null " +
            "       ELSE t.id " +
            "       END AS id, " +
            "   t.name, " +
            "   t.visibility, " +
            "   t.maxParticipants, " +
            "   t.startingTime, " +
            "   {name: g.name, _id: g._id} AS game "
    )
    List<TournamentNeo4jDTO> findCreatedTournaments(String username, String currentUsername);

    /**
     * Finds all tournaments a user participates in.
     *
     * @param username the username of the user
     * @return a list of tournaments the user participates in
     */
    @Query("MATCH (u:User {username: $username})-[:PARTICIPATES]->(t:Tournament)-[:IS_RELATED_TO]->(g:Game) " +
            "RETURN " +
            "   CASE " +
            "       WHEN t.visibility <> 'PUBLIC' AND  $username <> $currentUsername" +
            "       THEN null " +
            "       ELSE t.id " +
            "       END AS id, " +
            "   t.name, " +
            "   t.visibility, " +
            "   t.maxParticipants, " +
            "   t.startingTime, " +
            "   {name: g.name, _id: g._id} AS game "
    )
    List<TournamentNeo4jDTO> findParticipatedTournaments(String username);

    /**
     * Finds all tournaments won by a user.
     *
     * @param username the username of the user
     * @return a list of tournaments won by the user
     */
    @Query("MATCH (u:User {username: $username})-[:WON]->(t:Tournament)-[:IS_RELATED_TO]->(g:Game) " +
            "RETURN " +
            "   CASE " +
            "       WHEN t.visibility <> 'PUBLIC' AND  $username <> $currentUsername" +
            "       THEN null " +
            "       ELSE t.id " +
            "       END AS id, " +
            "   t.name, " +
            "   t.visibility, " +
            "   t.maxParticipants, " +
            "   t.startingTime, " +
            "   {name: g.name, _id: g._id} AS game "
    )
    List<TournamentNeo4jDTO> findWonTournaments(String username);

    /*================================ FEED =================================*/


    /**
     * Recent followers activity given a user (paginated)
     *
     * @param username the username of the user
     * @param skip the number of records to skip
     * @param limit the maximum number of records to return
     * @return a list of recent followers activity
     */
    @Query("MATCH (u:User {username: $username})<-[:FOLLOWS]-(follower:User)-[activity:LIKES|FOLLOWS|CREATED|PARTICIPATES|WON]->(object) " +
            "OPTIONAL MATCH (object)-[:IS_RELATED_TO]->(game:Game) " +
            "WITH " +
            "    follower.username AS followerUsername, " +
            "    activity, " +
            "    CASE " +
            "        WHEN activity:FOLLOWS THEN activity.since " +
            "        ELSE activity.timestamp " +
            "    END AS activityTime, " +
            "    CASE " +
            "        WHEN object:User THEN {username: object.username} " +
            "        WHEN object:Tournament THEN {name: object.name, _id: " +
            "           CASE WHEN object.visibility <> 'PUBLIC' THEN null ELSE object._id END, " +
            "           game: {name: game.name, _id: game._id}} " +
            "        ELSE {name: object.name, _id: object._id} " +
            "    END AS objectProperties " +
            "WHERE activityTime <= datetime() " +
            "RETURN " +
            "    followerUsername AS follower, " +
            "    activity, " +
            "    activityTime, " +
            "    objectProperties " +
            "ORDER BY activityTime DESC " +
            "SKIP $skip " +
            "LIMIT $limit")
    List<FollowersActivityDTO> getFollowedRecentActivities(String username, int skip, int limit);

    @Query("MATCH (u:User {username: $username})-[activity:LIKES|FOLLOWS|CREATED|PARTICIPATES|WON]->(object) " +
            "OPTIONAL MATCH (object)-[:IS_RELATED_TO]->(game:Game) " +
            "WITH " +
            "    activity, " +
            "    CASE " +
            "        WHEN activity:FOLLOWS THEN activity.since " +
            "        ELSE activity.timestamp " +
            "    END AS activityTime, " +
            "    CASE " +
            "        WHEN object:User THEN {username: object.username} " +
            "        WHEN object:Tournament THEN {name: object.name, _id: object._id, game: {name: game.name, _id: game._id}} " +
            "        ELSE {name: object.name, _id: object._id} " +
            "    END AS objectProperties " +
            "WHERE activityTime <= datetime() " +
            "RETURN " +
            "    followerUsername AS follower, " +
            "    activity, " +
            "    activityTime, " +
            "    objectProperties " +
            "ORDER BY activityTime DESC " +
            "SKIP $skip " +
            "LIMIT $limit")
    List<PersonalActivityDTO> getProfileRecentActivities(String username, int skip, int limit);

    /*============================ SUGGESTIONS ==============================*/

    /**
     * TODO Suggests games to a user based on the games they like.
     *
     * @param username the username of the user
     * @return a list of games suggested to the user
     */
    @Query("MATCH (u:User {username: $username})<-[:FOLLOWS]-(follower:User)-[:LIKES]->(g:Game) " +
            "WHERE NOT (u)-[:LIKES]->(g) " +
            "WITH u, g " +
            "MATCH (u)-[:LIKES]->(likedGame:Game) " +
            "WITH g, likedGame, " +
            "     [x IN g.categories WHERE x IN likedGame.categories] AS intersection, " +
            "     g.categories + [x IN likedGame.categories WHERE NOT x IN g.categories] AS union " +
            "WITH g, size(intersection) AS intersectionSize, size(union) AS unionSize " +
            "WHERE unionSize > 0 " +
            "WITH g, (1.0 * intersectionSize / unionSize) AS jaccardSimilarity " +
            "WHERE jaccardSimilarity > 0 " +
            "RETURN DISTINCT " +
            "   g.name AS gameName, " +
            "   g._id AS _id, " +
            "   g.yearReleased as yearReleased" +
            "   g.shortDescription AS shortDescription, " +
            "   jaccardSimilarity AS similarity" +
            "ORDER BY jaccardSimilarity DESC " +
            "LIMIT $limit")
    List<GameReccomendationDTO> getRecommendedGames(String username, int limit);

    /**
     * Suggerire utenti in base a quelli che segui. In particolare si prendono i follower dei follower
     * che non l'utente non segue già e si ordina per numero di follower in comune.
     *
     * @param username the username of the user
     * @return a list of users suggested to the user
     */
    @Query("MATCH (currentUser:User {username: $username})-[:FOLLOWS]->(followedUser:User)-[:FOLLOWS]->(suggestedUser:User)"+
            "WHERE NOT (currentUser)-[:FOLLOWS]->(suggestedUser) AND suggestedUser.username <> $username"+
            "RETURN suggestedUser.username AS username, COUNT(suggestedUser) AS commonFollowers"+
            "ORDER BY commonFollowers DESC"+
            "LIMIT 10")
    List<UserSuggestionDTO> suggestUsers(String username);

    //    Suggerimento di utenti che hanno gusti in comune con l'utente corrente
    @Query("CALL { " +
            "    MATCH (currentUser:User {username: $username})-[:LIKES]->(g:Game)<-[:LIKES]-(otherUser:User) " +
            "    WHERE NOT (currentUser)-[:FOLLOWS]->(otherUser) AND currentUser.username <> otherUser.username " +
            "    WITH collect(currentUser) + collect(otherUser) AS sourceNodes, collect(g) AS targetNodes " +
            "    CALL gds.graph.project( " +
            "      'userGameGraph', " +
            "      sourceNodes, " +
            "      targetNodes, " +
            "      { " +
            "        LIKES: { " +
            "          type: 'LIKES' " +
            "        } " +
            "      } " +
            "    ) " +
            "    YIELD graphName " +
            "} " +
            "CALL gds.nodeSimilarity.stream('userGameGraph') " +
            "YIELD node1, node2, similarity " +
            "WITH gds.util.asNode(node2) AS otherUser, similarity " +
            "WHERE gds.util.asNode(node1).username = $username " +
            "RETURN otherUser.username AS username, similarity " +
            "ORDER BY similarity DESC " +
            "LIMIT 10 " +
            "CALL gds.graph.drop('userGameGraph') YIELD graphName")
    List<UserSimilarityDTO> findSimilarUsersByGameTaste(String username);



    //Suggerimento di tornei
    //Tre livelli di suggerimento:
    //Alta Priorità: Tornei di giochi che piacciono all'utente e che sono organizzati  o partecipati da amici
    //Media Priorità: Tornei di giochi che piacciono all'utente
    //Bassa Priorità: Tornei a cui partecipano o sono amministrati da amici
    @Query("MATCH (user:User {username: $username})-[:FOLLOWS]->(friend:User)-[:PARTICIPATES|ADMINISTRATES]->(tournament:Tournament)-[:RELATED_TO]->(game:Game) " +
            "WHERE (user)-[:LIKES]->(game) " +
            "  AND tournament.visibility = 'Public' " +
            "  AND tournament.startingTime > datetime() " +
            "  AND NOT (user)-[:PARTICIPATES|ADMINISTRATES]->(tournament) " +
            "WITH tournament, 'HP' AS priority " +
            "UNION " +
            "MATCH (user:User {username: $username})-[:LIKES]->(game:Game)<-[:RELATED_TO]-(tournament:Tournament) " +
            "WHERE tournament.visibility = 'Public' " +
            "  AND tournament.startingTime > datetime() " +
            "  AND NOT (user)-[:PARTICIPATES|ADMINISTRATES]->(tournament) " +
            "WITH tournament, 'MP' AS priority " +
            "UNION " +
            "MATCH (user:User {username: $username})-[:FOLLOWS]->(friend:User)-[:PARTICIPATES|ADMINISTRATES]->(tournament:Tournament) " +
            "WHERE tournament.visibility = 'Public' " +
            "  AND tournament.startingTime > datetime() " +
            "  AND NOT (user)-[:PARTICIPATES|ADMINISTRATES]->(tournament) " +
            "WITH tournament, 'LP' AS priority " +
            "RETURN DISTINCT " +
            "  tournament.id AS id, " +
            "  tournament.name AS name, " +
            "  size((:User)-[:PARTICIPATES]->(tournament)) AS participantsCount, " +
            "  tournament.maxParticipants AS maxParticipants, " +
            "  tournament.visibility AS visibility, " +
            "  tournament.startingTime AS startingTime, " +
            "  head([(admin:User)-[:ADMINISTRATES]->(tournament) | admin.username]) AS administrator, " +
            "  priority " +
            "ORDER BY " +
            "  CASE priority " +
            "    WHEN 'HP' THEN 1 " +
            "    WHEN 'MP' THEN 2 " +
            "    WHEN 'LP' THEN 3 " +
            "  END " +
            "LIMIT 10")
    List<TournamentSuggestionDTO> suggestTournaments(String username);




    /*============================ ANALYTICS ==============================*/



}