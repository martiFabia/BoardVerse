package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.User.TournamentParticipantDTO;
import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;


public interface TournamentNeo4jRepository extends Neo4jRepository<TournamentNeo4j, String> {

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
    @Query("MATCH (u:User {username: $username}) " +
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
     * Updates  a tournament.
     *
     * @param tournamentId the ID of the tournament
     * @param newName the new name of the tournament
     * @param newVisibility the new visibility of the tournament
     * @param newMaxParticipants the new maximum number of participants in the tournament
     * @param newStartingTime the new starting time of the tournament
     * @return true if the tournament was updated, false otherwise
     */
    @Query(
            "MATCH (t:Tournament {id: $tournamentId}) " +
                    "WHERE t.startingTime > datetime() " +
                    "SET t.name = $newName, " +
                    "    t.visibility = $newVisibility, " +
                    "    t.maxParticipants = $newMaxParticipants, " +
                    "    t.startingTime = $newStartingTime " +
                    "RETURN count(t) > 0"
    )
    boolean updateTournament(String tournamentId, String newName, String newVisibility, int newMaxParticipants, String newStartingTime);

    /**
     * Removes a tournament.
     *
     * @param tournamentId the ID of the tournament to be removed
     * @return true if the tournament was removed, false otherwise
     */
    @Query("MATCH (t:Tournament {id: $tournamentId}) " +
            "DETACH DELETE t " +
            "RETURN count(t) > 0"
    )
    boolean removeTournament(String tournamentId);


    @Query("""
            MATCH (tournament:Tournament {_id: $tournamentId})<-[p:PARTICIPATES]-(user:User)
            RETURN
              user.username AS username,
              p.timestamp AS registrationTime
            ORDER BY
              CASE $sortBy
                WHEN 'registrationTime' THEN p.timestamp
                WHEN 'username' THEN user.username
              END ASC
            RETURN
              user.username AS username,
              p.timestamp AS registrationTime
    """)
    List<TournamentParticipantDTO> getParticipants(String tournamentId, String sortBy);


    /*====================================== ANALYTICS ======================================*/

    /**
     * Returns the difficulty of a tournament.
     *
     * @param tournamentId the ID of the tournament
     * @return the number of participants in the tournament
     */
    @Query("""
        MATCH (tournament:Tournament {_id: "$tournamentId})<-[:PARTICIPATES]-(participant:User)
        OPTIONAL MATCH (participant)-[:WON]->(wonTournament:Tournament)
        WITH tournament, participant, COUNT(wonTournament) AS wonCount
        OPTIONAL MATCH (participant)-[:PARTICIPATES]->(participatedTournament:Tournament)
        WITH tournament, participant, wonCount, COUNT(participatedTournament) AS totalCount
        WITH tournament, participant,
             CASE WHEN totalCount > 0 THEN (1.0 * wonCount / totalCount) ELSE 0 END AS winPercentage
        RETURN
          tournament._id AS tournamentId,
          tournament.name AS tournamentName,
          AVG(winPercentage) AS competitionDifficulty
    """)
    double tournamentDifficulty(String tournamentId);

    @Query("""
            MATCH (tournament:Tournament {_id: "03ef4779-92f9-4ea6-9e69-245f068d1860"})<-[:PARTICIPATES]-(participant:User)
            OPTIONAL MATCH (participant)-[:FOLLOWS]->(otherParticipant:User)
            WHERE (otherParticipant)-[:PARTICIPATES]->(tournament)
            WITH tournament, COUNT(otherParticipant) AS followCount, COLLECT(DISTINCT participant) AS participants
            WITH tournament, followCount, size(participants) AS participantCount,
                 (size(participants) * (size(participants) - 1)) AS maxPossibleFollows
            WITH tournament, followCount, participantCount, maxPossibleFollows,
                 CASE WHEN maxPossibleFollows > 0 THEN (1.0 * followCount / maxPossibleFollows) ELSE 0 END AS compactness
            RETURN
              compactness
    """)
    double tournamentSocialDensity();
}
