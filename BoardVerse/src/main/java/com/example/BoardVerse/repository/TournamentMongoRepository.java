package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.MostPlayedGameDTO;
import com.example.BoardVerse.DTO.Tournament.TournPreview;
import com.example.BoardVerse.model.MongoDB.Tournament;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.Date;
import java.util.List;

public interface TournamentMongoRepository extends MongoRepository<Tournament, String> {

    @Query("{ 'administrator': ?0 }")
    @Update("{ '$set': { 'administrator': ?1 } }")
    void updateAdministratorInTournaments(String oldUsername, String newUsername);

    @Query("{ 'winner': ?0 }")
    @Update("{ '$set': { 'winner': ?1 } }")
    void updateWinnerInTournaments(String oldUsername, String newUsername);

    //void updateAllowedInTournaments(String oldUsername, String newUsername);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.name': ?1 } }")
    void updateGameNameInTournaments(String gameId, String gameName);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.yearReleased': ?1 } }")
    void updateGameYearInTournaments(String gameId, Integer gameYear);


    void deleteByAdministrator(String username);
    void deleteByGameId(String gameId);



    @Query(value = "{ '$or': [ { 'visibility': 'PUBLIC' }, { 'visibility': 'PRIVATE', 'allowed': ?1 } ], 'game._id': ?0 }",
            sort = "{ 'startingTime': -1 }",
            fields = "{ '_id':1, 'name': 1, 'type':1, 'startingTime': 1, 'numParticipants': 1, 'minParticipants': 1, 'maxParticipants': 1, 'administrator':1, 'winner': 1 }")
    Slice<TournPreview> findByGameOrderByStartingTimeDesc(String gameId, String username, Pageable pageable);


    @Aggregation(pipeline = {
            "{ $match: { 'startingTime': { $gte: ?0, $lt: ?1 }, 'numParticipants': { $gt: 10 } } }",
            "{ $addFields: { 'weight': { '$switch': { 'branches': [ " +
                    "{ 'case': { '$eq': ['$visibility', 'PUBLIC'] }, 'then': 2 }, " +
                    "{ 'case': { '$eq': ['$visibility', 'INVITE'] }, 'then': 1.5 }, " +
                    "{ 'case': { '$eq': ['$visibility', 'PRIVATE'] }, 'then': 1 } ], " +
                    "'default': 1 } }, " +
                    "'weightedParticipants': { '$multiply': ['$numParticipants', " +
                    "{ '$switch': { 'branches': [ " +
                    "{ 'case': { '$eq': ['$visibility', 'PUBLIC'] }, 'then': 2 }, " +
                    "{ 'case': { '$eq': ['$visibility', 'INVITE'] }, 'then': 1.5 }, " +
                    "{ 'case': { '$eq': ['$visibility', 'PRIVATE'] }, 'then': 1 } ], " +
                    "'default': 1 } }] } } }",
            "{ $group: { '_id': '$game._id', 'name': { '$first': '$game.name' }, 'yearReleased': { '$first': '$game.yearReleased' }, " +
                    "'totalWeightedParticipants': { '$sum': '$weightedParticipants' }, 'totalWeight': { '$sum': '$weight' } } }",
            "{ $addFields: { 'averageWeightedParticipants': { '$divide': ['$totalWeightedParticipants', '$totalWeight'] } } }",
            "{ $sort: { 'averageWeightedParticipants': -1 } }",
            "{ $limit: 10 }",
            "{ $project: { '_id': 0, 'gameID': '$_id', 'name': 1, 'yearReleased': 1, 'averageWeightedParticipants': 1 } }"
    })
    List<MostPlayedGameDTO> findTop10GamesWithHighestAverageParticipation(Date startDate, Date endDate);







}
