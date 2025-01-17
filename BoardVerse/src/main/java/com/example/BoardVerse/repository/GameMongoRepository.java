package com.example.BoardVerse.repository;

import com.example.BoardVerse.model.MongoDB.GameMongo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GameMongoRepository extends MongoRepository<GameMongo, String> {

    Optional<GameMongo> findByNameAndYearReleased(String name, int yearReleased);
    List<GameMongo> findByName(String name);
    List<GameMongo> findByCategoriesContaining(String category);

}
