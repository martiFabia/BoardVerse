package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.aggregation.CountryAggregation;
import com.example.BoardVerse.DTO.User.aggregation.MonthlyReg;
import com.example.BoardVerse.model.MongoDB.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<User, String> {

    // Trova un utente per username
    Optional<User> findByUsername(String username);

    @Query( value = "{ 'username': { '$regex': ?0, '$options': 'i' } }",
            fields = "{ 'username': 1, 'email': 1 }" )
    Slice<UserDTO> findByUsernameContaining(String username, Pageable pageable);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query(value = "{ '_id': { '$in': ?0 } }", fields = "{ '_id': 1 }")
    List<String> findUsersById(List<String> ids);


    @Query(value = "{ 'mostRecentReviews.game._id': ?0 }")
    @Update("{ '$set': { 'mostRecentReviews.$.game.name': ?1 } }")
    void updateGameNameInMostRecentReviews(String gameId, String gameName);

    @Query(value = "{ 'mostRecentReviews.game._id': ?0 }")
    @Update("{ '$set': { 'mostRecentReviews.$.game.yearReleased': ?1 } }")
    void updateGameYearInMostRecentReviews(String gameId, Integer gameYear);


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
            "{ '$match': { '$expr': { '$eq': [ { '$year': '$registeredDate' }, ?0 ] } } }",
            "{ '$group': { '_id': { '$month': '$registeredDate' }, 'registrations': { '$sum': 1 } } }",
            "{ '$sort': { '_id': 1 } }"
    })
    List<MonthlyReg> monthlyRegistrations(Integer year);





}
