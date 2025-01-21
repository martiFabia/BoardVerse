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
                    + "      { 'location.state': :#{#state} } "
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


/*
    @Aggregation(pipeline = {
            // (1) Calcolo dell'età con $dateDiff (MongoDB 5.0+)
            "{ $addFields: { " +
                    "    age: { " +
                    "      $dateDiff: { " +
                    "        startDate: \"$authorBirthDate\", " +
                    "        endDate: \"$$NOW\", " +
                    "        unit: \"year\" " +
                    "      } " +
                    "    } " +
                    "} }",

            // (2) Filtro per età [10..99]
            "{ $match: { " +
                    "    age: { $gte: 10, $lte: 99 } " +
                    "} }",

            // (3) Creiamo il campo 'ageBracket' (es. "10-19", "20-29", etc.)
            "{ $addFields: { " +
                    "    ageBracket: { " +
                    "      $concat: [ " +
                    "        { $toString: { $multiply: [ 10, { $floor: { $divide: [\"$age\", 10] } } ] } }, " +
                    "        \"-\", " +
                    "        { $toString: { " +
                    "          $add: [ " +
                    "            { $multiply: [ 10, { $floor: { $divide: [\"$age\", 10] } } ] }, " +
                    "            9 " +
                    "          ] " +
                    "        } } " +
                    "      ] " +
                    "    } " +
                    "} }",

            // (4) Proiettiamo i campi su nomi "puliti" (senza punti)
            //     gameId, gameName, yearReleased.
            "{ $project: { " +
                    "    ageBracket: 1, " +       // ci serve dopo
                    "    rating: 1, " +           // ci serve per calcolare la media
                    "    gameId: \"$game._id\", " +
                    "    gameName: \"$game.name\", " +
                    "    yearReleased: \"$game.yearReleased\" " +
                    "} }",

            // (5) Raggruppiamo per (ageBracket, gameId) e calcoliamo la media rating
            "{ $group: { " +
                    "    _id: { " +
                    "      ageBracket: \"$ageBracket\", " +
                    "      gameId: \"$gameId\", " +
                    "      gameName: \"$gameName\", " +
                    "      yearReleased: \"$yearReleased\" " +
                    "    }, " +
                    "    averageRating: { $avg: \"$rating\" } " +
                    "} }",

            // (6) Ordiniamo per fascia d'età asc, poi per averageRating disc
            "{ $sort: { \"_id.ageBracket\": 1, \"averageRating\": -1 } }",

            // (7) Per ogni fascia d'età, prendiamo il "primo" (miglior) gioco
            "{ $group: { " +
                    "    _id: \"$_id.ageBracket\", " +
                    "    gameId: { $first: \"$_id.gameId\" }, " +
                    "    gameName: { $first: \"$_id.gameName\" }, " +
                    "    yearReleased: { $first: \"$_id.yearReleased\" }, " +
                    "    bestAvgRating: { $first: \"$averageRating\" } " +
                    "} }",

            // (8) (Facoltativo) Ordine finale per fascia di età
            "{ $sort: { \"_id\": 1 } }"
    })
    List<BestGameAgeDTO> findBestGameByAgeBrackets();

 */



}
