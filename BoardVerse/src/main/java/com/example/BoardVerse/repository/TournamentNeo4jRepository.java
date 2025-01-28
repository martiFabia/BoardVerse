package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Tournament.ExtendedTournamentNeo4jInfo;
import com.example.BoardVerse.DTO.User.TournamentParticipantDTO;
import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
import lombok.NonNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;


public interface TournamentNeo4jRepository extends Neo4jRepository<TournamentNeo4j, String> {

    @Query("MATCH (t:Tournament {_id: $tournamentId}) " +
            "RETURN t")
    Optional<TournamentNeo4j> findById(@NonNull String tournamentId);

    /**
     * Removes a tournament.
     *
     * @param tournamentId the ID of the tournament to be removed
     */
    @Query(
        "MATCH (t:Tournament {_id: $tournamentId}) " +
        "DETACH DELETE t "
    )
    void deleteById(@NonNull String tournamentId);

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
    @Query(
        "MATCH (u:User {username: $username}) " +
        "MATCH (g:Game {_id: $gameId}) " +
        "MERGE (t:Tournament {_id: $tournamentId}) " +
        "ON CREATE SET t.name = $name, " +
        "              t.visibility = $visibility, " +
        "              t.maxParticipants = $maxParticipants, " +
        "              t.startingTime = datetime($startingTime) " +
        "MERGE (u)-[:CREATED {timestamp: datetime()}]->(t) " +
        "MERGE (t)-[:IS_RELATED_TO]->(g) "
    )
    void save(String username, String tournamentId, String name, String visibility, int maxParticipants, String startingTime, String gameId);

    /**
     * Updates a tournament.
     * @param tournamentId the ID of the tournament
     * @param name the name of the tournament
     * @param visibility the visibility of the tournament
     * @param maxParticipants the maximum number of participants in the tournament
     * @param startingTime the starting time of the tournament
     * @param winner the username of the winner of the tournament
     */
    @Query("""
        MATCH (tournament:Tournament {_id: $tournamentId})
        SET
          tournament.name = $name,
          tournament.startingTime = datetime($startingTime),
          tournament.maxParticipants = $maxParticipants,
          tournament.visibility = $visibility
        WITH tournament
        OPTIONAL MATCH (user:User {username: $winner})
        FOREACH (_ IN CASE WHEN user IS NOT NULL THEN [1] ELSE [] END |
          MERGE (user)-[won:WON]->(tournament)
          ON CREATE SET won.timestamp = datetime()
        )
        WITH tournament, user
        MATCH (otherUser:User)-[won:WON]->(tournament)
        WHERE $winner IS NULL OR otherUser.username <> $winner
        DELETE won
    """)
    void updateTournament(String tournamentId, String name, String visibility, int maxParticipants, String startingTime, String winner);

    /**
     * Returns the participants of a tournament.
     * @param tournamentId the ID of the tournament
     * @param sortBy the field to sort the participants by
     * @return the participants of the tournament
     */
    @Query("""
        MATCH (tournament:Tournament {_id: $tournamentId})<-[p:PARTICIPATES]-(user:User)
        RETURN
          user.username AS username,
          p.timestamp AS registrationTime
        ORDER BY
          CASE $sortBy
            WHEN 'time' THEN p.timestamp
            WHEN 'alphabetical' THEN user.username
          END ASC
    """)
    List<TournamentParticipantDTO> getParticipants(String tournamentId, String sortBy, int pageSize, int pageNumber);

    /**
     * Participates in a tournament.
     *
     * @param username the username of the user participating in the tournament
     * @param tournamentId the ID of the tournament
     * @return true if the user was added to the tournament, false otherwise
     */
    @Query("""
            MATCH (u:User {username: $username})
            MATCH (t:Tournament {_id: $tournamentId})
            WHERE NOT EXISTS {
                MATCH (t)<-[:PARTICIPATES]-()
                WITH COUNT(*) AS participantCount
                WHERE participantCount >= t.maxParticipants
            }
              AND t.startingTime > datetime()
            MERGE (u)-[:PARTICIPATES {timestamp: datetime()}]->(t)
            RETURN count(t) > 0
    """)
    boolean addParticipantToTournament(String username, String tournamentId);

    /**
     * Removes a user from a tournament.
     *
     * @param username the username of the user to be removed from the tournament
     * @param tournamentId the ID of the tournament
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {_id: $tournamentId}) " +
            "MATCH (u)-[r:PARTICIPATES]->(t) " +
            "DELETE r"
    )
    void removeParticipantFromTournament(String username, String tournamentId);

    /**
     * Wins a tournament.
     *
     * @param username the username of the user winning the tournament
     * @param tournamentId the ID of the tournament
     */
    @Query("MATCH (u:User {username: $username}) " +
            "MATCH (t:Tournament {_id: $tournamentId}) " +
            "MERGE (u)-[:WON {timestamp: datetime()}]->(t)" +
            "RETURN count(t) > 0")
    void setWinner(String username, String tournamentId);


    /*====================================== ANALYTICS ======================================*/

    /**
     * Returns the difficulty of a tournament.
     *
     * @param tournamentId the ID of the tournament
     * @return the number of participants in the tournament
     */
    @Query("""
        MATCH (tournament:Tournament {_id: $tournamentId})<-[:PARTICIPATES]-(participant:User)
        OPTIONAL MATCH (participant)-[:WON]->(wonTournament:Tournament)
        WITH tournament, participant, COUNT(wonTournament) AS wonCount
        OPTIONAL MATCH (participant)-[:PARTICIPATES]->(participatedTournament:Tournament)
        WITH tournament, participant, wonCount, COUNT(participatedTournament) AS totalCount
        WITH tournament,
             CASE
                 WHEN COUNT(participant) = 0 THEN 0
                 ELSE AVG(
                     CASE
                         WHEN totalCount > 0 THEN (1.0 * wonCount / totalCount)
                         ELSE 0
                     END
                 )
             END AS competitionDifficulty
        RETURN
          competitionDifficulty
    """)
    double getTournamentDifficulty(String tournamentId);

    @Query("""
        MATCH (tournament:Tournament {_id: $tournamentId})<-[:PARTICIPATES]-(participant:User)
        OPTIONAL MATCH (participant)-[:FOLLOWS]->(otherParticipant:User)
        WHERE (otherParticipant)-[:PARTICIPATES]->(tournament)
        WITH tournament, COUNT(otherParticipant) AS followCount, COLLECT(DISTINCT participant) AS participants
        WITH tournament, followCount, size(participants) AS participantCount,
             (size(participants) * (size(participants) - 1)) AS maxPossibleFollows
        WITH tournament, followCount, participantCount, maxPossibleFollows,
             CASE
                 WHEN participantCount = 0 THEN 0
                 WHEN maxPossibleFollows > 0 THEN (1.0 * followCount / maxPossibleFollows)
                 ELSE 0  // Default a 0
             END AS compactness
        RETURN
          compactness
    """)
    double getTournamentSocialDensity(String tournamentId);

}
