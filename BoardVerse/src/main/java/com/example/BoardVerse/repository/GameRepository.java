package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.MongoDB.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends MongoRepository<Game, String> {

    Optional<Game> findById(String id);
    List<Game> findByName(String name);
    List<Game> findByCategoriesContaining(String category);

}
