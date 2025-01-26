package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.User.TournamentParticipantDTO;
import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;


public interface TournamentNeo4jRepository extends Neo4jRepository<TournamentNeo4j, String> {

    /**
     * Creates a tournamentMongo.
     *
     * @param username the username of the userMongo creating the tournamentMongo
     * @param tournamentId the ID of the tournamentMongo
     * @param name the name of the tournamentMongo
     * @param visibility the visibility of the tournamentMongo
     * @param maxParticipants the maximum number of participants in the tournamentMongo
     * @param startingTime the starting time of the tournamentMongo
     * @param gameId the ID of the game related to the tournamentMongo
     */
    @Query("MATCH (u:UserMongo {username: $username}) " +
            "MATCH (g:Game {_id: $gameId}) " +
            "MERGE (t:TournamentMongo {_id: $tournamentId}) " +
            "ON CREATE SET t.name = $name, " +
            "              t.visibility = $visibility, " +
            "              t.maxParticipants = $maxParticipants, " +
            "              t.startingTime = datetime($startingTime) " +
            "MERGE (u)-[:CREATED {timestamp: datetime()}]->(t) " +
            "MERGE (t)-[:IS_RELATED_TO]->(g) "
    )
    void save(String username, String tournamentId, String name, String visibility, int maxParticipants, String startingTime, String gameId);

    /**
     * Updates  a tournamentMongo.
     *
     * @param tournamentId the ID of the tournamentMongo
     * @param newName the new name of the tournamentMongo
     * @param newVisibility the new visibility of the tournamentMongo
     * @param newMaxParticipants the new maximum number of participants in the tournamentMongo
     * @param newStartingTime the new starting time of the tournamentMongo
     * @return true if the tournamentMongo was updated, false otherwise
     */
    @Query(
            "MATCH (t:TournamentMongo {id: $tournamentId}) " +
                    "WHERE t.startingTime > datetime() " +
                    "SET t.name = $newName, " +
                    "    t.visibility = $newVisibility, " +
                    "    t.maxParticipants = $newMaxParticipants, " +
                    "    t.startingTime = $newStartingTime " +
                    "RETURN count(t) > 0"
    )
    boolean updateTournament(String tournamentId, String newName, String newVisibility, int newMaxParticipants, String newStartingTime);

    /**
     * Removes a tournamentMongo.
     *
     * @param tournamentId the ID of the tournamentMongo to be removed
     * @return true if the tournamentMongo was removed, false otherwise
     */
    @Query("MATCH (t:TournamentMongo {id: $tournamentId}) " +
            "DETACH DELETE t " +
            "RETURN count(t) > 0"
    )
    boolean removeTournament(String tournamentId);


    @Query("""
            MATCH (tournamentMongo:TournamentMongo {_id: $tournamentId})<-[p:PARTICIPATES]-(userMongo:UserMongo)
            RETURN
              userMongo.username AS username,
              p.timestamp AS registrationTime
            ORDER BY
              CASE $sortBy
                WHEN 'registrationTime' THEN p.timestamp
                WHEN 'username' THEN userMongo.username
              END ASC
            RETURN
              userMongo.username AS username,
              p.timestamp AS registrationTime
    """)
    List<TournamentParticipantDTO> getParticipants(String tournamentId, String sortBy);


    /*====================================== ANALYTICS ======================================*/

    /**
     * Returns the difficulty of a tournamentMongo.
     *
     * @param tournamentId the ID of the tournamentMongo
     * @return the number of participants in the tournamentMongo
     */
    @Query("""
        MATCH (tournamentMongo:TournamentMongo {_id: "$tournamentId})<-[:PARTICIPATES]-(participant:UserMongo)
        OPTIONAL MATCH (participant)-[:WON]->(wonTournament:TournamentMongo)
        WITH tournamentMongo, participant, COUNT(wonTournament) AS wonCount
        OPTIONAL MATCH (participant)-[:PARTICIPATES]->(participatedTournament:TournamentMongo)
        WITH tournamentMongo, participant, wonCount, COUNT(participatedTournament) AS totalCount
        WITH tournamentMongo, participant,
             CASE WHEN totalCount > 0 THEN (1.0 * wonCount / totalCount) ELSE 0 END AS winPercentage
        RETURN
          tournamentMongo._id AS tournamentId,
          tournamentMongo.name AS tournamentName,
          AVG(winPercentage) AS competitionDifficulty
    """)
    double tournamentDifficulty(String tournamentId);

    @Query("""
            MATCH (tournamentMongo:TournamentMongo {_id: "03ef4779-92f9-4ea6-9e69-245f068d1860"})<-[:PARTICIPATES]-(participant:UserMongo)
            OPTIONAL MATCH (participant)-[:FOLLOWS]->(otherParticipant:UserMongo)
            WHERE (otherParticipant)-[:PARTICIPATES]->(tournamentMongo)
            WITH tournamentMongo, COUNT(otherParticipant) AS followCount, COLLECT(DISTINCT participant) AS participants
            WITH tournamentMongo, followCount, size(participants) AS participantCount,
                 (size(participants) * (size(participants) - 1)) AS maxPossibleFollows
            WITH tournamentMongo, followCount, participantCount, maxPossibleFollows,
                 CASE WHEN maxPossibleFollows > 0 THEN (1.0 * followCount / maxPossibleFollows) ELSE 0 END AS compactness
            RETURN
              compactness
    """)
    double tournamentSocialDensity();
}
