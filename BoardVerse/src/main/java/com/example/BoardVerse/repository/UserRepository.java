package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.model.MongoDB.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    // Trova un utente per username
    Optional<User> findByUsername(String username);

    @Query( value = "{ 'username': { '$regex': ?0, '$options': 'i' } }",
            fields = "{ 'username': 1, 'email': 1 }" )
    Slice<UserDTO> findByUsernameContaining(String username, Pageable pageable);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

}
