package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.BestGameAgeDTO;
import com.example.BoardVerse.DTO.Game.GamePreviewDTO;
import com.example.BoardVerse.DTO.Game.RatingDetails;
import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.DTO.Review.ReviewUserDTO;
import com.example.BoardVerse.model.MongoDB.Review;
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
    @Query(value = "{ 'game.id': ?0 }",
            fields = "{ 'id': 1, 'authorUsername': 1, 'rating': 1, 'content': 1, 'postDate': 1 }")
    Slice<ReviewInfo> findByGameId(String gameId, Pageable pageable);

    @Aggregation(pipeline = {
            "{ $match: { 'authorUsername': ?0 } }",
            "{ $sort: { 'postDate': -1 } }",
            "{ $project: { " +
                    "id: 1, " +
                    "game: '$game.name', " +
                    "yearReleased: '$game.yearReleased', " +
                    "rating: 1, " +
                    "content: 1, " +
                    "postDate: 1 " +
                    "} }"
    })
    Slice<ReviewUserDTO> findByAuthorUsername(String username, Pageable pageable);


    void deleteByGameId(String gameId);

    // Trova la prossima recensione più recente per un utente
    Optional<Review> findFirstByAuthorUsernameOrderByPostDateDesc(String username);

    // Aggiorna il campo username in tutte le recensioni di un utente
    @Query("{ 'authorUsername': ?0 }")
    @Update("{ '$set': { 'authorUsername': ?1 } }")
    void updateUsernameInReviews(String oldUsername, String newUsername);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.name': ?1 } }")
    void updateGameNameInReviews(String gameId, String gameName);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.yearReleased': ?1 } }")
    void updateGameYearInReviews(String gameId, Integer gameYear);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.shortDescription': ?1 } }")
    void updateGameShortDescInReviews(String gameId, String shortDesc);


    @Aggregation(pipeline = {
            "{ '$match': { 'game._id': ?0, 'rating': { '$ne': null } } }", // Filtra per game._id e rating non null
            "{ '$project': { 'rating': 1, 'roundedRating': { '$round': ['$rating', 0] } } }", // Mantieni rating e calcola roundedRating
            "{ '$facet': { " +
                    "'stats': [ " +
                    "{ '$group': { '_id': null, 'avgRating': { '$avg': '$rating' }, 'stdDeviation': { '$stdDevPop': '$rating' } } } " +
                    "], " +
                    "'distribution': [ " +
                    "{ '$group': { '_id': '$roundedRating', 'count': { '$sum': 1 } } }, " +
                    "{ '$sort': { '_id': 1 } }, " +
                    "{ '$project': { 'rating': '$_id', 'count': 1, '_id': 0 } } " +
                    "] " +
                    "} }",
            "{ '$project': { " +
                    "'avgRating': { '$arrayElemAt': ['$stats.avgRating', 0] }, " +
                    "'stdDeviation': { '$arrayElemAt': ['$stats.stdDeviation', 0] }, " +
                    "'distribution': '$distribution' " +
                    "} }"
    })
    RatingDetails findRatingDetailsByGameId(String gameId);


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
            "{ $match: { rating: { $ne: null } } }", // Escludi recensioni con rating null
            "{ $addFields: { age: { $dateDiff: { startDate: \"$authorBirthDate\", endDate: \"$$NOW\", unit: \"year\" } } } }",
            "{ $match: { age: { $gte: 10, $lte: 99 } } }",
            "{ $addFields: { ageBracket: { $concat: [ { $toString: { $multiply: [10, { $floor: { $divide: [\"$age\", 10] } }] } }, \"-\", { $toString: { $add: [ { $multiply: [10, { $floor: { $divide: [\"$age\", 10] } }] }, 9 ] } } ] } } }",
            "{ $project: { ageBracket: 1, rating: 1, game: \"$game._id\", name: \"$game.name\", yearReleased: \"$game.yearReleased\" } }",
            "{ $group: { _id: { ageBracket: \"$ageBracket\", game: \"$game\", name: \"$name\", yearReleased: \"$yearReleased\" }, averageRating: { $avg: \"$rating\" }, totalReviews: { $sum: 1 } } }",
            "{ $sort: { \"_id.ageBracket\": 1, \"averageRating\": -1 } }",
            "{ $group: { _id: \"$_id.ageBracket\", gameID: { $first: \"$_id.game\" }, name: { $first: \"$_id.name\" }, yearReleased: { $first: \"$_id.yearReleased\" }, bestAvgRating: { $first: \"$averageRating\" }, totalReviews: { $sum: \"$totalReviews\" } } }", // Somma il numero di recensioni totali
            "{ $sort: { \"_id\": 1 } }"
    })
    List<BestGameAgeDTO> findBestGameByAgeBrackets();

}
