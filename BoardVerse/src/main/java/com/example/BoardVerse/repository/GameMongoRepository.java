package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.GameRankPreviewDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface GameMongoRepository extends MongoRepository<GameMongo, String> {

    Optional<GameMongo> findByNameAndYearReleased(String name, int yearReleased);


    @Query( value = "{ 'name': { '$regex': ?0, '$options': 'i' } }",
            fields = "{ 'id': 1, 'name': 1, 'yearReleased': 1, 'shortDescription' : 1, 'averageRating': 1 }" )
    Slice<GameRankPreviewDTO> findByNameContaining(String name, Pageable pageable);

    //lista giochi filtrati per anno di rilascio, meccaniche e categoria
    @Query("{ '$and': [ "
            + "  { '$or': [ { '$expr': { '$eq': [ :#{#yearReleased}, null ] } }, { 'yearReleased': :#{#yearReleased} } ] }, "
            + "  { '$or': [ { '$expr': { '$eq': [ :#{#categories}, null ] } }, { 'categories': :#{#categories} } ] }, "
            + "  { '$or': [ { '$expr': { '$eq': [ :#{#mechanics}, null ] } }, { 'mechanics': :#{#mechanics} } ] } "
            + "] }")
    Slice<GameRankPreviewDTO> findGamesByFilters(Integer yearReleased, String categories, String mechanics, Pageable pageable);






}
