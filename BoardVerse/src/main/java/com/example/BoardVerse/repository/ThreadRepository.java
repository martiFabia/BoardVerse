package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Thread.ThreadPreviewDTO;
import com.example.BoardVerse.DTO.Thread.ThreadPreviewGameDTO;
import com.example.BoardVerse.model.MongoDB.ThreadMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.Date;

public interface ThreadRepository extends MongoRepository<ThreadMongo, String> {

    @Aggregation(pipeline = {
            "{ $match: { $and: [ "
                    + "  { $or: [ "
                    + "      { $expr: { $eq: [ :#{#gameName}, null ] } }, "
                    + "      { $expr: { $eq: [ :#{#gameName}, '' ] } }, "
                    + "      { 'game.name': { $regex: :#{#gameName}, $options: 'i' } } "
                    + "    ] }, "
                    + "  { $or: [ { $expr: { $eq: [ :#{#yearReleased}, null ] } }, { 'game.yearReleased': :#{#yearReleased} } ] }, "
                    + "  { $or: [ { $expr: { $eq: [ :#{#startPostDate}, null ] } }, { 'postDate': { $gte: :#{#startPostDate} } } ] }, "
                    + "  { $or: [ { $expr: { $eq: [ :#{#endPostDate}, null ] } }, { 'postDate': { $lte: :#{#endPostDate} } } ] }, "
                    + "  { $or: [ { $expr: { $eq: [ :#{#tag}, null ] } }, { 'tag': :#{#tag} } ] } "
                    + "] } }",
            "{ $addFields: { messageCount: { $size: '$messages' } } }", // Aggiungi il numero di messaggi
            "{ $project: { "
                    + "id: $_id, "
                    + "tag: 1, "
                    + "game: 1, "
                    + "lastPostDate: 1, "
                    + "authorUsername: 1, "
                    + "postDate: 1, "
                    + "content: 1, "
                    + "messageCount: 1 "
                    + "} }"
    })
    Slice<ThreadPreviewDTO> findFilteredThread(
            String gameName,
            Integer yearReleased,
            Date startPostDate,
            Date endPostDate,
            String tag,
            Pageable pageable);

    @Query("{ 'game.id': ?0 }")
    void deleteAllByGameId(String gameId);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.name': ?1, 'game.yearReleased': ?2 } }")
    void updateGameInfoById(String gameId, String gameName, Integer yearReleased);

    @Query("{'game.id': ?0}")
    @Update("{ '$set': { 'game.yearReleased': ?1 } }")
    void updateYearByGameId(String gameId, Integer yearReleased);

    @Query("{ 'authorUsername': ?0 }")
    @Update("{ '$set': { 'authorUsername': ?1 } }")
    void updateThreadAuthorUsername(String username, String newUsername);

    @Query("{ 'messages.authorUsername': ?0 }")
    @Update("{ '$set': { 'messages.$[].authorUsername': ?1 } }")
    void updateMessageAuthorUsername(String username, String newUsername);

    @Query("{ 'messages.replyTo': { '$exists': true }, 'messages.replyTo.username': ?0 }")
    @Update("{ '$set': { 'messages.$[].replyTo.username': ?1 } }")
    void updateReplyToUsername(String username, String newUsername);


    @Query(value = "{ 'game.id': ?0 }",
            fields = "{ 'id': 1, 'tag': 1, 'lastPostDate': 1, " +
                    "'authorUsername': 1, 'postDate': 1, 'content': 1, 'messageCount': { $size: '$messages' } }")
    Slice<ThreadPreviewGameDTO> findByGameId(String gameId, Pageable pageable);


}

