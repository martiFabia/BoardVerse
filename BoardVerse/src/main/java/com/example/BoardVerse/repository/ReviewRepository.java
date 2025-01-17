package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.MongoDB.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    // Trova tutte le recensioni di un gioco
    List<Review> findByGameId(String gameId);

    // Trova tutte le recensioni di un utente
    List<Review> findByAuthorUsername(String authorUsername);

    void deleteByAuthorUsername(String username);

    // Trova la prossima recensione più recente per un gioco
    Optional<Review> findFirstByGameIdOrderByCreatedAtDesc(String gameId);

    // Trova la prossima recensione più recente per un utente
    Optional<Review> findFirstByAuthorUsernameOrderByCreatedAtDesc(String username);

    // Aggiorna il campo username in tutte le recensioni di un utente
    @Query("{ 'authorUsername': ?0 }")
    @Update("{ '$set': { 'authorUsername': ?1 } }")
    void updateUsernameInReviews(String oldUsername, String newUsername);


}
