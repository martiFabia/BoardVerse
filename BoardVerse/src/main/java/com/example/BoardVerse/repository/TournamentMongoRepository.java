package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.MostPlayedGameDTO;
import com.example.BoardVerse.DTO.Tournament.TournPreview;
import com.example.BoardVerse.model.MongoDB.Tournament;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface TournamentMongoRepository extends MongoRepository<Tournament, String> {

    @Query(value = "{ 'game._id': ?0 }", sort = "{ 'startingTime': -1 }",
            fields = "{ '_id':1, 'name': 1, 'type':1, 'startingTime': 1, 'numParticipants': 1, 'minParticipants': 1, 'maxParticipants': 1, 'administrator':1,'winner': 1 }")
    Slice<TournPreview> findByGameOrderByStartingTimeDesc(String gameId, Pageable pageable);

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
            "{ $project: { '_id': 0, 'gameId': '$_id', 'name': 1, 'yearReleased': 1, 'averageWeightedParticipants': 1 } }"
    })
    List<MostPlayedGameDTO> findTop10GamesWithHighestAverageParticipation(Date startDate, Date endDate);







}
