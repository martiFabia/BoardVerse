package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.User.FollowersActivityDTO;
import com.example.BoardVerse.DTO.Game.GameSuggestionDTO;
import com.example.BoardVerse.DTO.User.PersonalActivityDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentSuggestionDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentNeo4jDTO;
import com.example.BoardVerse.DTO.User.UserFollowRecommendationDTO;
import com.example.BoardVerse.DTO.User.UserTastesSuggestionDTO;
import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserNeo4j entities.
 * Provides methods to perform various actions and queries related to users, games, and tournaments.
 */
public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4j, String> {

    /**
     * Creates a userMongo, if the new username is not already taken.
     * @param oldUsername the username of the userMongo
     * @param newUsername the username of the userMongo
     * @return true if the userMongo was created, false otherwise
     */
    @Query("MATCH (u:UserMongo {username: $oldUsername}) " +
            "OPTIONAL MATCH (existing:UserMongo {username: $newUsername}) " +
            "WITH u, existing " +
            "WHERE existing IS NULL " +
            "SET u.username = $newUsername " +
            "RETURN count(u) > 0")
    boolean updateUserNeo4jByUsername(String oldUsername, String newUsername);

    /**
     *  Finds a userMongo by username.
     * @param username the username of the userMongo
     * @return the userMongo with the given username
     */
    Optional<UserNeo4j> findByUsername(String username);

    /**
     * Removes a userMongo from the database.
     *
     * @param username the username of the userMongo
     * @return true if the userMongo was removed, false otherwise
     */
    @Query("MATCH (u:UserMongo {username: $username}) " +
            "DETACH DELETE u" +
            "RETURN count(u) > 0")
    boolean removeUser(String username);


    /*============================ USER-GAME ACTIONS ==============================*/

    /**
     * Adds a game to the userMongo's favorites.
     *
     * @param username the username of the userMongo
     * @param gameId the ID of the game to be added to favorites
     * @return true if the game was added to favorites, false otherwise
     */
    @Query("MATCH (u:UserMongo {username: $username}) " +
            "MATCH (g:Game {_id: $gameId}) " +
            "MERGE (u)-[l:LIKES]->(g) " +
            "ON CREATE SET l.timestamp = datetime() " +
            "RETURN count(g) > 0"
    )
    boolean addLikeToGame(String username, String gameId);

    /**
     * Removes a game from the userMongo's favorites.
     *
     * @param username the username of the userMongo
     * @param gameId the ID of the game to be removed from favorites
     * @return true if the game was removed from favorites, false otherwise
     */
    @Query("MATCH (u:UserMongo {username: $username}) " +
            "MATCH (g:Game {_id: $gameId}) " +
            "MATCH (u)-[r:LIKES]->(g) " +
            "DELETE r " +
            "RETURN count(g) > 0"
    )
    boolean removeLikeFromGame(String username, String gameId);


    /*============================ USER-USER ACTIONS ==============================*/

    /**
     * Follows another userMongo.
     *
     * @param followerId the ID of the userMongo who wants to follow
     * @param followedId the ID of the userMongo to be followed
     * @return true if the follow operation was successful, false otherwise
     */
    @Query("MATCH (follower:UserMongo {username: $followerId}), MATCH (followed:UserMongo {username: $followedId}) " +
            "MERGE (follower)-[f:FOLLOWS]->(followed) " +
            "ON CREATE SET f.since = datetime() " +
            "RETURN count(follower) > 0"
    )
    boolean followUser(String followerId, String followedId);

    /**
     * Unfollows another userMongo.
     *
     * @param username the username of the userMongo who wants to unfollow
     * @param usernameToUnfollow the username of the userMongo to be unfollowed
     * @return true if the unfollow operation was successful, false otherwise
     */
    @Query("MATCH (u:UserMongo {username: $username}) " +
            "MATCH (u2:UserMongo {username: $usernameToUnfollow}) " +
            "MATCH (u)-[r:FOLLOWS]->(u2) " +
            "DELETE r " +
            "RETURN count(r) > 0")
    boolean unfollowUser(String username, String usernameToUnfollow);


    /*============================ USER-TOURNAMENT ACTIONS ==============================*/

    /**
     * Participates in a tournamentMongo.
     *
     * @param username the username of the userMongo participating in the tournamentMongo
     * @param tournamentId the ID of the tournamentMongo
     * @return true if the userMongo participated in the tournamentMongo, false otherwise
     */
    @Query("""
            MATCH (u:UserMongo {username: $username})
            MATCH (t:TournamentMongo {_id: $tournamentId})
            WHERE NOT EXISTS {
                MATCH (t)<-[:PARTICIPATES]-()
                WITH COUNT(*) AS participantCount
                WHERE participantCount >= t.maxParticipants
            }
              AND t.startingTime > datetime()
            MERGE (u)-[:PARTICIPATES {timestamp: datetime()}]->(t)
            RETURN count(u) > 0                
    """)
    boolean participateToTournament(String username, String tournamentId);

    /**
     * Removes a userMongo from a tournamentMongo.
     *
     * @param username the username of the userMongo to be removed from the tournamentMongo
     * @param tournamentId the ID of the tournamentMongo
     * @return true if the userMongo was removed from the tournamentMongo, false otherwise
     */
    @Query("MATCH (u:UserMongo {username: $username}) " +
            "MATCH (t:TournamentMongo {_id: $tournamentId}) " +
            "MATCH (u)-[r:PARTICIPATES]->(t) " +
            "DELETE r" +
            "RETURN count(r) > 0"
    )
    boolean removeUserFromTournament(String username, String tournamentId);

    /**
     * Wins a tournamentMongo.
     *
     * @param username the username of the userMongo winning the tournamentMongo
     * @param tournamentId the ID of the tournamentMongo
     * @return true if the userMongo won the tournamentMongo, false otherwise
     */
    @Query("MATCH (u:UserMongo {username: $username}) " +
            "MATCH (t:TournamentMongo {_id: $tournamentId}) " +
            "MERGE (u)-[:WON {timestamp: datetime()}]->(t)" +
            "RETURN count(t) > 0")
    boolean winTournament(String username, String tournamentId);


    /*============================ FINDS ==============================*/

    /**
     * Finds all games liked by a userMongo.
     *
     * @param username the username of the userMongo
     * @param sortOrder the order in which the games should be returned
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of games liked by the userMongo
     */
    @Query("MATCH (u:UserMongo {username: $username})-[:LIKES]->(g:Game) " +
            "RETURN g " +
            "ORDER BY CASE WHEN $sortOrder = 'alphabetical' THEN g.name ELSE g.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<GameNeo4j> findLikedGames(String username, String sortOrder, int pageSize, int pageNumber);

    /**
     * Finds all users followed by a userMongo.
     *
     * @param username the username of the userMongo
     * @param sortOrder the order in which the games should be returned
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of users followed by the userMongo
     */
    @Query("MATCH (u:UserMongo {username: $username})-[f:FOLLOWS]->(u2:UserMongo) " +
            "RETURN u2.username " +
            "ORDER BY CASE WHEN $sortOrder = 'alphabetical' THEN u2.username ELSE f.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<UserNeo4j> findFollowedUsers(String username, String sortOrder, int pageSize, int pageNumber);

    /**
     * Finds all users following a userMongo.
     *
     * @param username the username of the userMongo
     * @return a list of users following the userMongo
     */
    @Query("MATCH (u:UserMongo {username: $username})<-[f:FOLLOWS]-(u2:UserMongo) " +
            "RETURN u2.username " +
            "ORDER BY CASE WHEN $sortOrder = 'alphabetical' THEN u2.username ELSE f.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<UserNeo4j> findFollowers(String username, String sortOrder, int pageSize, int pageNumber);

    /**
     * Finds all tournaments created by a userMongo.
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments created by the userMongo
     */
    @Query("MATCH (u:UserMongo {username: $username})-[c:CREATED]->(t:TournamentMongo)-[:IS_RELATED_TO]->(g:Game) " +
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
        "   {name: g.name, _id: g._id} AS game " +
        "ORDER BY c.timestamp DESC " +
        "SKIP $pageSize * ($pageNumber - 1) " +
        "LIMIT $pageSize")
    List<TournamentNeo4jDTO> findCreatedTournaments(String username, String currentUsername, int pageSize, int pageNumber);

    /**
     * Finds all tournaments a userMongo participates in.
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments the userMongo participates in
     */
    @Query("MATCH (u:UserMongo {username: $username})-[p:PARTICIPATES]->(t:TournamentMongo)-[:IS_RELATED_TO]->(g:Game) " +
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
        "   {name: g.name, _id: g._id} AS game " +
        "ORDER BY p.timestamp DESC " +
        "SKIP $pageSize * ($pageNumber - 1) " +
        "LIMIT $pageSize")
    List<TournamentNeo4jDTO> findParticipatedTournaments(String username, String currentUsername, int pageSize, int pageNumber);

    /**
     * Finds all tournaments won by a userMongo.
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments won by the userMongo
     */
    @Query("MATCH (u:UserMongo {username: $username})-[w:WON]->(t:TournamentMongo)-[:IS_RELATED_TO]->(g:Game) " +
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
        "   {name: g.name, _id: g._id} AS game " +
        "ORDER BY w.timestamp DESC " +
        "SKIP $pageSize * ($pageNumber - 1) " +
        "LIMIT $pageSize")
    List<TournamentNeo4jDTO> findWonTournaments(String username, String currentUsername, int pageSize, int pageNumber);


    /*================================ ACTIVITY =================================*/

    /**
     * Recent followers activity given a userMongo
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of recent followers activity
     */
    @Query("MATCH (u:UserMongo {username: $username})<-[:FOLLOWS]-(follower:UserMongo)-[activity:LIKES|FOLLOWS|CREATED|PARTICIPATES|WON]->(object) " +
            "OPTIONAL MATCH (object)-[:IS_RELATED_TO]->(game:Game) " +
            "WITH " +
            "    follower.username AS followerUsername, " +
            "    activity, " +
            "    CASE " +
            "        WHEN activity:FOLLOWS THEN activity.since " +
            "        ELSE activity.timestamp " +
            "    END AS activityTime, " +
            "    CASE " +
            "        WHEN object:UserMongo THEN {username: object.username} " +
            "        WHEN object:TournamentMongo THEN {name: object.name, _id: " +
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
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<FollowersActivityDTO> getFollowedRecentActivities(String username, int pageSize, int pageNumber);

    /**
     * Recent followers activity given a userMongo
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of recent followers activity
     */
    @Query("MATCH (u:UserMongo {username: $username})-[activity:LIKES|FOLLOWS|CREATED|PARTICIPATES|WON]->(object) " +
            "OPTIONAL MATCH (object)-[:IS_RELATED_TO]->(game:Game) " +
            "WITH " +
            "    activity, " +
            "    CASE " +
            "        WHEN activity:FOLLOWS THEN activity.since " +
            "        ELSE activity.timestamp " +
            "    END AS activityTime, " +
            "    CASE " +
            "        WHEN object:UserMongo THEN {username: object.username} " +
            "        WHEN object:TournamentMongo THEN {name: object.name, _id: object._id, game: {name: game.name, _id: game._id}} " +
            "        ELSE {name: object.name, _id: object._id} " +
            "    END AS objectProperties " +
            "WHERE activityTime <= datetime() " +
            "RETURN " +
            "    followerUsername AS follower, " +
            "    activity, " +
            "    activityTime, " +
            "    objectProperties " +
            "ORDER BY activityTime DESC " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<PersonalActivityDTO> getProfileRecentActivities(String username, int pageSize, int pageNumber);

    /*============================ SUGGESTIONS ==============================*/

    /**
     * Suggests games to a userMongo based on the games they like.
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of games suggested to the userMongo
     */
    @Query("MATCH (u:UserMongo {username: $username})<-[:FOLLOWS]-(follower:UserMongo)-[:LIKES]->(g:Game) " +
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
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<GameSuggestionDTO> getGamesRecommendation(String username, int pageSize, int pageNumber);

    /**
     * Suggests users to a userMongo based on the users they follow.
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of users suggested to the userMongo
     */
    @Query("MATCH (currentUser:UserMongo {username: $username})-[:FOLLOWS]->(followedUser:UserMongo)-[:FOLLOWS]->(suggestedUser:UserMongo)"+
            "WHERE NOT (currentUser)-[:FOLLOWS]->(suggestedUser) AND suggestedUser.username <> $username"+
            "RETURN suggestedUser.username AS username, COUNT(suggestedUser) AS commonFollowers"+
            "ORDER BY commonFollowers DESC"+
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<UserFollowRecommendationDTO> getUsersRecommendationBySimilarNetwork(String username, int pageSize, int pageNumber);

    /**
     * Suggests users to a userMongo based on the games they like.
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of users suggested to the userMongo
     */
    @Query("""
            MATCH (userMongo:UserMongo {username: $username})-[:FOLLOWS]->(follower:UserMongo)-[:FOLLOWS]->(suggestedUser:UserMongo)
            WHERE NOT (userMongo)-[:FOLLOWS]->(suggestedUser) AND suggestedUser <> userMongo
            WITH DISTINCT userMongo, suggestedUser
            OPTIONAL MATCH (userMongo)-[:LIKES]->(game:Game)<-[:LIKES]-(suggestedUser)
            WITH DISTINCT userMongo, suggestedUser, COLLECT(DISTINCT game) AS commonGames
            MATCH (userMongo)-[:LIKES]->(allGames:Game)
            WITH DISTINCT userMongo, suggestedUser, commonGames, COLLECT(DISTINCT allGames) AS allUserGames
            MATCH (suggestedUser)-[:LIKES]->(suggestedGames:Game)
            WITH DISTINCT suggestedUser, commonGames, allUserGames, COLLECT(DISTINCT suggestedGames) AS allSuggestedGames
            WITH DISTINCT suggestedUser,
                 size(commonGames) AS intersectionSize,
                 size(apoc.coll.union(allUserGames, allSuggestedGames)) AS unionSize,
                 (1.0 * size(commonGames) / size(apoc.coll.union(allUserGames, allSuggestedGames))) AS likeSimilarity
            RETURN DISTINCT
              suggestedUser.username AS username,
              likeSimilarity
            ORDER BY likeSimilarity DESC
            SKIP $pageSize * ($pageNumber - 1)
            LIMIT $pageSize
    """)
    List<UserTastesSuggestionDTO> getUsersRecommendationBySimilarTastes(@Param("username") String username, @Param("pageSize") int pageSize, @Param("pageNumber") int pageNumber);


    /**
     * Suggests tournaments to a userMongo based on the games they like, the games liked by the users they follow, and the tournaments they participate in.
     *
     * @param username the username of the userMongo
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments suggested to the userMongo
     */
    @Query("""
            // Find tournaments related to games liked by the userMongo
            MATCH (userMongo:UserMongo {username: $username})-[:PARTICIPATES]->(userTournament:TournamentMongo)-[:IS_RELATED_TO]->(game:Game)
                
            // Find tournaments related to games liked by the users followed by the userMongo
            MATCH (game)<-[:IS_RELATED_TO]-(suggestedTournament:TournamentMongo)
            WHERE NOT (userMongo)-[:PARTICIPATES]->(suggestedTournament)
              AND suggestedTournament.startingTime > datetime()
              AND suggestedTournament.visibility = 'PUBLIC'
              AND NOT EXISTS {
                MATCH (suggestedTournament)<-[:PARTICIPATES]-(:UserMongo)
                WITH COUNT(*) AS participantCount
                WHERE participantCount >= suggestedTournament.maxParticipants
              } // Only public tournaments with available spots not yet started
                
            // Followed users that participate in the suggested tournaments
            MATCH (userMongo)-[:FOLLOWS]->(follower:UserMongo)-[:PARTICIPATES]->(suggestedTournament)
            WITH suggestedTournament, COUNT(DISTINCT follower) AS followerCount
                
            // Total number of participants in the suggested tournaments
            MATCH (suggestedTournament)<-[:PARTICIPATES]-(participant:UserMongo)
            WITH suggestedTournament, followerCount, COUNT(DISTINCT participant) AS participantCount
                
            // Game related to the suggested tournamentMongo
            MATCH (suggestedTournament)-[:IS_RELATED_TO]->(relatedGame:Game)
            WITH suggestedTournament, followerCount, participantCount,
                 relatedGame.name AS gameName,
                 relatedGame.yearReleased AS gameYearReleased,
                 relatedGame._id AS gameId
                
            // Return the suggested tournaments
            RETURN DISTINCT
              suggestedTournament._id AS id,
              suggestedTournament.name AS name,
              suggestedTournament.startingTime AS startingTime,
              suggestedTournament.maxParticipants AS maxParticipants,
              followerCount,
              participantCount,
              gameName,
              gameYearReleased,
              gameId
            ORDER BY followerCount DESC, suggestedTournament.startingTime ASC
            SKIP $pageSize * ($pageNumber - 1)
            LIMIT $pageSize
    """)
    List<TournamentSuggestionDTO> getTournamentsRecommendation(String username, int pageSize, int pageNumber);

}