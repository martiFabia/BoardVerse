package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.aggregation.CountryAggregation;
import com.example.BoardVerse.DTO.User.aggregation.MonthlyReg;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<UserMongo, String> {

    // Trova un utente per username
    Optional<UserMongo> findByUsername(String username);

    @Query( value = "{ 'username': { '$regex': ?0, '$options': 'i' } }",
            fields = "{ 'username': 1, 'email': 1 }" )
    Slice<UserDTO> findByUsernameContaining(String username, Pageable pageable);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query(value = "{ 'username': { '$in': ?0 } }", fields = "{ '_id': 1 }")
    List<String> findUsersByUsername(List<String> usernames);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.name': ?1, 'game.yearReleased': ?2, 'game.shortDescription': ?3 } }")
    void updateGameInfoById(String gameId, String gameName, Integer yearReleased, String shortDescription);

    @Query(value = "{'mostRecentReviews.game._id': ?0 }")
    @Update("{ '$pull': { 'mostRecentReviews': { 'game._id': ?0 } } }")
    void deleteGameFromMostRecentReviews(String gameId);


    @Aggregation(pipeline = {
            "{ '$match': { 'location.country': { '$ne': null }, 'location.stateOrProvince': { '$ne': null }} }",
            "{ '$group': { '_id': { 'country': '$location.country', 'state': '$location.stateOrProvince'}, 'count': { '$sum': 1 } } }",
            "{ '$group': { '_id': '$_id.country', 'states': { '$push': { 'state': '$_id.state', 'count': '$count' } } } }",
            "{ '$sort': { '_id': 1 } }",  // Ordinamento alfabetico su country
            "{ '$project': { '_id': 0, 'country': '$_id', 'states': 1 } }"
    })
    Slice<CountryAggregation> countUsersByLocationHierarchy(Pageable pageable);


    @Aggregation(pipeline = {
            "{ '$match': { 'registeredDate': { '$gte': ?0, '$lt': ?1 } } }",
            "{ '$group': { '_id': { '$month': '$registeredDate'  }, 'registrations': { '$sum': 1 } } }",
            "{ '$sort': { '_id': 1 } }"
    })
    List<MonthlyReg> monthlyRegistrations(Instant startDate, Instant endDate);






}
