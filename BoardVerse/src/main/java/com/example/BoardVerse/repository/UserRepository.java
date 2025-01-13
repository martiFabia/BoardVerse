package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.MongoDB.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {

    // Trova un utente per username
    Optional<User> findByUsername(String username);

    // Controlla se un utente esiste con un determinato username
    boolean existsByUsername(String username);

    // Trova tutti gli utenti che sono admin
    // List<User> findByIsAdmin(boolean isAdmin);
}
