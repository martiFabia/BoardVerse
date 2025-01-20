package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.GamePreviewDTO;
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
    Slice<Review> findByGameIdOrderByPostDateDesc(String gameId, Pageable pageable);

    // Trova tutte le recensioni di un utente
    List<Review> findByAuthorUsername(String authorUsername);

    void deleteByAuthorUsername(String username);

    // Trova la prossima recensione più recente per un utente
    Optional<Review> findFirstByAuthorUsernameOrderByPostDateDesc(String username);

    // Aggiorna il campo username in tutte le recensioni di un utente
    @Query("{ 'authorUsername': ?0 }")
    @Update("{ '$set': { 'authorUsername': ?1 } }")
    void updateUsernameInReviews(String oldUsername, String newUsername);

    @Aggregation(pipeline = {
            "{ $match: { 'postDate': { $gte: ?0, $lte: ?1 } } }", // Filtro per intervallo di date
            "{ $group: { " +
                    "      _id: '$game._id', " + // Raggruppa per gameId
                    "      name: { $first: '$game.name' }, " +
                    "      yearReleased: { $first: '$game.yearReleased' }, " +
                    "      shortDescription: { $first: '$game.shortDescription' }, " +
                    "      averageRating: { $avg: '$rating' } " +
                    "} }",
            "{ $sort: { averageRating: -1 } }" ,
            "{ $project: { " +
                    "      _id: 1, " +
                    "      name: 1, " +
                    "      shortDescription: 1, " +
                    "      yearReleased: 1, " +
                    "      averageRating: { $round: ['$averageRating', 2] } " + //Arrotonda a due cifre decimali
                    "} }"
    })
    Slice<GamePreviewDTO> findAverageRatingByGameBetweenDates(Date startDate, Date endDate, Pageable pageable);





}
