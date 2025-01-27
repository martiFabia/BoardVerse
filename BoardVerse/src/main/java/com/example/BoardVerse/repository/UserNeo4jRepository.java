package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.GameLikedDTO;
import com.example.BoardVerse.DTO.User.*;
import com.example.BoardVerse.DTO.Game.GameSuggestionDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentSuggestionDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentNeo4jDTO;
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
     *  Finds a user by username.
     * @param username the username of the user
     * @return the user with the given username
     */
    Optional<UserNeo4j> findByUsername(String username);

    /**
     * Removes a user from the database.
     *
     * @param username the username of the user
     * @return true if the user was removed, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "DETACH DELETE u " +
            "RETURN count(u) > 0"
    )
    boolean deleteByUsername(String username);


    /*============================ USER-GAME ACTIONS ==============================*/

    /**
     * Adds a game to the user's favorites.
     *
     * @param username the username of the user
     * @param gameId the ID of the game to be added to favorites
     * @return true if the game was added to favorites, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {_id: $gameId}) " +
            "MERGE (u)-[l:LIKES]->(g) " +
            "ON CREATE SET l.timestamp = datetime() " +
            "RETURN count(g) > 0"
    )
    boolean addLikeToGame(String username, String gameId);

    /**
     * Removes a game from the user's favorites.
     *
     * @param username the username of the user
     * @param gameId the ID of the game to be removed from favorites
     * @return true if the game was removed from favorites, false otherwise
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (g:Game {_id: $gameId}) " +
            "MATCH (u)-[r:LIKES]->(g) " +
            "DELETE r " +
            "RETURN count(g) > 0"
    )
    boolean removeLikeFromGame(String username, String gameId);


    /*============================ USER-USER ACTIONS ==============================*/

    /**
     * Follows another user.
     *
     * @param followerId the ID of the user who wants to follow
     * @param followedId the ID of the user to be followed
     * @return true if the user was followed, false otherwise
     */
    @Query("""
            MATCH (follower:User {username: $followerId})
            MATCH (followed:User {username: $followedId})
            MERGE (follower)-[f:FOLLOWS]->(followed)
            ON CREATE SET f.since = datetime()
            RETURN count(follower) + count(followed) > 1;
    """)
    boolean followUser(String followerId, String followedId);

    /**
     * Unfollows another user.
     *
     * @param username the username of the user who wants to unfollow
     * @param usernameToUnfollow the username of the user to be unfollowed
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (u2:User {username: $usernameToUnfollow}) " +
            "MATCH (u)-[r:FOLLOWS]->(u2) " +
            "DELETE r " +
            "RETURN count(r) > 0"
    )
    boolean unfollowUser(String username, String usernameToUnfollow);


    /*============================ FINDS ==============================*/

    /**
     * Finds all games liked by a user.
     *
     * @param username the username of the user
     * @param sortBy the order in which the games should be returned
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of games liked by the user
     */
    @Query("MATCH (u:User {username: $username})-[l:LIKES]->(g:Game) " +
            "RETURN g._id AS id, g.name AS name, g.yearReleased AS yearReleased, g.shortDescription AS shortDescription, l.timestamp AS likedAt " +
            "ORDER BY CASE WHEN $sortBy = 'alphabetical' THEN g.name ELSE l.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<GameLikedDTO> getLikedGames(String username, String sortBy, int pageSize, int pageNumber);

    /**
     * Finds all users followed by a user.
     *
     * @param username the username of the user
     * @param sortOrder the order in which the games should be returned
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of users followed by the user
     */
    @Query("MATCH (u:User {username: $username})-[f:FOLLOWS]->(u2:User) " +
            "RETURN u2.username AS username, f.since AS since " +
            "ORDER BY CASE WHEN $sortOrder = 'alphabetical' THEN u2.username ELSE f.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<UserFollowsDTO> getFollowing(String username, String sortOrder, int pageSize, int pageNumber);

    /**
     * Finds all users following a user.
     *
     * @param username the username of the user
     * @return a list of users following the user
     */
    @Query("MATCH (u:User {username: $username})<-[f:FOLLOWS]-(u2:User) " +
            "RETURN u2.username AS username, f.since AS since " +
            "ORDER BY CASE WHEN $sortBy = 'alphabetical' THEN u2.username ELSE f.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<UserFollowsDTO> getFollowers(String username, String sortBy, int pageSize, int pageNumber);

    /**
     * Finds all tournaments created by a user.
     *
     * @param username the username of the user
     * @param currentUsername the username of the current user
     * @param sortBy the order in which the tournaments should be returned
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments created by the user
     */
    @Query("MATCH (u:User {username: $username})-[c:CREATED]->(t:Tournament)-[:IS_RELATED_TO]->(g:Game) " +
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
        "   {id: g._id, name: g.name, yearReleased: g.yearReleased} AS game " +
        "ORDER BY CASE WHEN $sortBy = 'desc' THEN c.timestamp DESC ELSE c.timestamp END " +
        "SKIP $pageSize * ($pageNumber - 1) " +
        "LIMIT $pageSize")
    List<TournamentNeo4jDTO> getCreatedTournaments(String username, String currentUsername, String sortBy, int pageSize, int pageNumber);

    /**
     * Finds all tournaments a user has participated in.
     *
     * @param username the username of the user
     * @param currentUsername the username of the current user
     * @param sortBy the order in which the tournaments should be returned
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments created by the user
     */
    @Query("MATCH (u:User {username: $username})-[c:PARTICIPATES]->(t:Tournament)-[:IS_RELATED_TO]->(g:Game) " +
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
            "   {id: g._id, name: g.name, yearReleased: g.yearReleased} AS game " +
            "ORDER BY CASE WHEN $sortBy = 'desc' THEN c.timestamp DESC ELSE c.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize")
    List<TournamentNeo4jDTO> getParticipatedTournaments(String username, String currentUsername, String sortBy, int pageSize, int pageNumber);

    /**
     * Finds all tournaments created by a user.
     *
     * @param username the username of the user
     * @param currentUsername the username of the current user
     * @param sortBy the order in which the tournaments should be returned
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments created by the user
     */
    @Query("MATCH (u:User {username: $username})-[c:WON]->(t:Tournament)-[:IS_RELATED_TO]->(g:Game) " +
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
            "   {id: g._id, name: g.name, yearReleased: g.yearReleased} AS game " +
            "ORDER BY CASE WHEN $sortBy = 'desc' THEN c.timestamp DESC ELSE c.timestamp END " +
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize")
    List<TournamentNeo4jDTO> getWonTournaments(String username, String currentUsername, String sortBy, int pageSize, int pageNumber);


    /*================================ ACTIVITY =================================*/

    /**
     * Recent followers activity given a user
     *
     * @param username the username of the user
     * @param pageSize the number of records to return
     * @param pageNumber the page number
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
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<FollowersActivityDTO> getFollowedActivity(String username, int pageSize, int pageNumber);

    /**
     * Recent followers activity given a user
     *
     * @param username the username of the user
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of recent followers activity
     */
    @Query("""
            MATCH (u:User {username: $username})-[activity:LIKES|FOLLOWS|CREATED|PARTICIPATES|WON]->(object)
            OPTIONAL MATCH (object)-[:IS_RELATED_TO]->(game:Game)
            WITH\s
                activity,\s
                CASE\s
                    WHEN activity:FOLLOWS THEN activity.since\s
                    ELSE activity.timestamp\s
                END AS activityTime,
                CASE\s
                    WHEN object:User THEN {username: object.username}\s
                    WHEN object:Tournament THEN {id: object._id, name: object.name, game: { id: game._id, name: game.name}}\s
                    ELSE {id: object._id, name: object.name}\s
                END AS activityProperties
            WHERE activityTime <= datetime()
            RETURN\s
                type(activity) AS activityType,
                activityProperties,
                activityTime
            ORDER BY activityTime DESC
            SKIP $pageSize * ($pageNumber - 1)
            LIMIT $pageSize
    """)
    List<PersonalActivityDTO> getPersonalActivity(String username, int pageSize, int pageNumber);


    /*============================ SUGGESTIONS ==============================*/

    /**
     * Suggests games to a user based on the games they like.
     *
     * @param username the username of the user
     * @param pageSize the number of records to return
     * @param pageNumber the page number
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
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<GameSuggestionDTO> getGamesRecommendation(String username, int pageSize, int pageNumber);

    /**
     * Suggests users to a user based on the users they follow.
     *
     * @param username the username of the user
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of users suggested to the user
     */
    @Query("MATCH (currentUser:User {username: $username})-[:FOLLOWS]->(followedUser:User)-[:FOLLOWS]->(suggestedUser:User)"+
            "WHERE NOT (currentUser)-[:FOLLOWS]->(suggestedUser) AND suggestedUser.username <> $username"+
            "RETURN suggestedUser.username AS username, COUNT(suggestedUser) AS commonFollowers"+
            "ORDER BY commonFollowers DESC"+
            "SKIP $pageSize * ($pageNumber - 1) " +
            "LIMIT $pageSize"
    )
    List<UserFollowRecommendationDTO> getUsersRecommendationBySimilarNetwork(String username, int pageSize, int pageNumber);

    /**
     * Suggests users to a user based on the games they like.
     *
     * @param username the username of the user
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of users suggested to the user
     */
    @Query("""
            MATCH (user:User {username: $username})-[:FOLLOWS]->(follower:User)-[:FOLLOWS]->(suggestedUser:User)
            WHERE NOT (user)-[:FOLLOWS]->(suggestedUser) AND suggestedUser <> user
            WITH DISTINCT user, suggestedUser
            OPTIONAL MATCH (user)-[:LIKES]->(game:Game)<-[:LIKES]-(suggestedUser)
            WITH DISTINCT user, suggestedUser, COLLECT(DISTINCT game) AS commonGames
            MATCH (user)-[:LIKES]->(allGames:Game)
            WITH DISTINCT user, suggestedUser, commonGames, COLLECT(DISTINCT allGames) AS allUserGames
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
     * Suggests tournaments to a user based on the games they like, the games liked by the users they follow, and the tournaments they participate in.
     *
     * @param username the username of the user
     * @param pageSize the number of records to return
     * @param pageNumber the page number
     * @return a list of tournaments suggested to the user
     */
    @Query("""
            // Find tournaments related to games liked by the user
            MATCH (user:User {username: $username})-[:PARTICIPATES]->(userTournament:Tournament)-[:IS_RELATED_TO]->(game:Game)
                
            // Find tournaments related to games liked by the users followed by the user
            MATCH (game)<-[:IS_RELATED_TO]-(suggestedTournament:Tournament)
            WHERE NOT (user)-[:PARTICIPATES]->(suggestedTournament)
              AND suggestedTournament.startingTime > datetime()
              AND suggestedTournament.visibility = 'PUBLIC'
              AND NOT EXISTS {
                MATCH (suggestedTournament)<-[:PARTICIPATES]-(:User)
                WITH COUNT(*) AS participantCount
                WHERE participantCount >= suggestedTournament.maxParticipants
              } // Only public tournaments with available spots not yet started
                
            // Followed users that participate in the suggested tournaments
            MATCH (user)-[:FOLLOWS]->(follower:User)-[:PARTICIPATES]->(suggestedTournament)
            WITH suggestedTournament, COUNT(DISTINCT follower) AS followerCount
                
            // Total number of participants in the suggested tournaments
            MATCH (suggestedTournament)<-[:PARTICIPATES]-(participant:User)
            WITH suggestedTournament, followerCount, COUNT(DISTINCT participant) AS participantCount
                
            // Game related to the suggested tournament
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