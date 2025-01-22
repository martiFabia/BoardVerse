package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.aggregation.CountryAggregation;
import com.example.BoardVerse.model.MongoDB.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<User, String> {

    // Trova un utente per username
    Optional<User> findByUsername(String username);

    @Query( value = "{ 'username': { '$regex': ?0, '$options': 'i' } }",
            fields = "{ 'username': 1, 'email': 1 }" )
    Slice<UserDTO> findByUsernameContaining(String username, Pageable pageable);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Aggregation(pipeline = {
            "{ '$match': { 'location.country': { '$ne': null }, 'location.stateOrProvince': { '$ne': null }} }",
            "{ '$group': { '_id': { 'country': '$location.country', 'state': '$location.stateOrProvince'}, 'count': { '$sum': 1 } } }",
            "{ '$group': { '_id': '$_id.country', 'states': { '$push': { 'state': '$_id.state', 'count': '$count' } } } }",
            "{ '$sort': { '_id': 1 } }",  // Ordinamento alfabetico su country
            "{ '$project': { '_id': 0, 'country': '$_id', 'states': 1 } }"
    })
    Slice<CountryAggregation> countUsersByLocationHierarchy(Pageable pageable);


}
