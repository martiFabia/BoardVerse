package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.BestGameAgeDTO;
import com.example.BoardVerse.DTO.Game.GamePreviewDTO;
import com.example.BoardVerse.model.MongoDB.Review;
import com.mongodb.lang.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;


import java.util.Date;
import java.util.List;
import java.util.Optional;


public interface ReviewRepository extends MongoRepository<Review, String> {
    // Trova tutte le recensioni di un gioco
    Slice<Review> findByGameId(String gameId, Pageable pageable);

    // Trova tutte le recensioni di un utente
    List<Review> findByAuthorUsername(String authorUsername);

    void deleteByAuthorUsername(String username);

    // Trova la prossima recensione più recente per un utente
    Optional<Review> findFirstByAuthorUsernameOrderByPostDateDesc(String username);

    // Aggiorna il campo username in tutte le recensioni di un utente
    @Query("{ 'authorUsername': ?0 }")
    @Update("{ '$set': { 'authorUsername': ?1 } }")
    void updateUsernameInReviews(String oldUsername, String newUsername);


    //classifica giochi filtrati per postDate e location e aggregati per gameId
    @Aggregation(pipeline = {
            "{ '$match': { 'postDate': { '$gte': ?0, '$lte': ?1 } } }",
            "{ '$match': { '$and': [ "
                    + "  { '$or': [ "
                    + "      { '$expr': { '$eq': [ :#{#country}, null ] } }, "
                    + "      { 'location.country': :#{#country} } "
                    + "    ] }, "
                    + "  { '$or': [ "
                    + "      { '$expr': { '$eq': [ :#{#state}, null ] } }, "
                    + "      { 'location.stateOrProvince': :#{#state} } "
                    + "    ] }, "
                    + "  { '$or': [ "
                    + "      { '$expr': { '$eq': [ :#{#city}, null ] } }, "
                    + "      { 'location.city': :#{#city} } "
                    + "    ] } "
                    + "] } }",
            "{ '$group': { "
                    + "  '_id': '$game._id', "
                    + "  'name': { '$first': '$game.name' }, "
                    + "  'yearReleased': { '$first': '$game.yearReleased' }, "
                    + "  'shortDescription': { '$first': '$game.shortDescription' }, "
                    + "  'averageRating': { '$avg': '$rating' } "
                    + "} }",
            "{ '$sort': { 'averageRating': -1 } }",
            "{ '$project': { "
                    + "  '_id': 1, "
                    + "  'name': 1, "
                    + "  'shortDescription': 1, "
                    + "  'yearReleased': 1, "
                    + "  'averageRating': { '$round': [ '$averageRating', 2 ] } "
                    + "} }"
    })
    Slice<GamePreviewDTO> findAverageRatingByPostDateLocation(Date startDate, Date endDate, String country, String state, String city, Pageable pageable);


    @Aggregation(pipeline = {
            "{ $addFields: { age: { $dateDiff: { startDate: \"$authorBirthDate\", endDate: \"$$NOW\", unit: \"year\" } } } }",
            "{ $match: { age: { $gte: 10, $lte: 99 } } }",
            "{ $addFields: { ageBracket: { $concat: [ { $toString: { $multiply: [10, { $floor: { $divide: [\"$age\", 10] } }] } }, \"-\", { $toString: { $add: [ { $multiply: [10, { $floor: { $divide: [\"$age\", 10] } }] }, 9 ] } } ] } } }",
            "{ $project: { ageBracket: 1, rating: 1, game: \"$game._id\", name: \"$game.name\", yearReleased: \"$game.yearReleased\" } }",
            "{ $group: { _id: { ageBracket: \"$ageBracket\", game: \"$game\", name: \"$name\", yearReleased: \"$yearReleased\" }, averageRating: { $avg: \"$rating\" } } }",
            "{ $sort: { \"_id.ageBracket\": 1, \"averageRating\": -1 } }",
            "{ $group: { _id: \"$_id.ageBracket\", gameID: { $first: \"$_id.game\" }, name: { $first: \"$_id.name\" }, yearReleased: { $first: \"$_id.yearReleased\" }, bestAvgRating: { $first: \"$averageRating\" } } }",
            "{ $sort: { \"_id\": 1 } }"
    })
    List<BestGameAgeDTO> findBestGameByAgeBrackets();




}
