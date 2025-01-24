package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;


public interface TournamentNeo4jRepository extends Neo4jRepository<TournamentNeo4j, String> {

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
                    "SET t.name = $newName, " +
                    "   t.visibility = $newVisibility, " +
                    "   t.maxParticipants = $newMaxParticipants, " +
                    "   t.startingTime = $newStartingTime " +
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

}
