package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.MongoDB.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    // Trova tutte le recensioni di un gioco
    List<Review> findByGameId(String gameId);

    // Trova tutte le recensioni di un utente
    List<Review> findByAuthorUsername(String authorUsername);

}
