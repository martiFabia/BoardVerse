package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Game.BestGameThreadDTO;
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


    @Query(value = "{ '$and': [ "
            + "  { 'game._id': :#{#gameId} }, "
            + "  { '$or': [ "
            + "     { '$expr': { '$eq': [ :#{#tag}, null ] } }, "
            + "     { 'tag': :#{#tag} } "
            + "  ] } "
            + "] }",
            fields = "{ 'id': 1, 'tag': 1, 'lastPostDate': 1, " +
                    "'authorUsername': 1, 'postDate': 1, 'content': 1, 'messageCount': { $size: '$messages' } }")
    Slice<ThreadPreviewGameDTO> findByGameId(String gameId, String tag, Pageable pageable);





    @Aggregation(pipeline = {
            // Filtra i messaggi dell'ultimo mese
            "{ $addFields: { messages: { $filter: { input: '$messages', as: 'message', cond: { $and: [ { $gte: ['$$message.postDate', ?0 ] }, { $lte: ['$$message.postDate', ?1] } ] } } } } }",

            // Calcola il peso per ogni messaggio
            "{ $addFields: { messages: { $map: { input: '$messages', as: 'message', in: { $let: { vars: { maxDate: ?1, minDate: ?0, dateDif: { $subtract: [?1, ?0] } }, in: { $mergeObjects: ['$$message', { weight: { $add: [0.1, { $divide: [ { $multiply: [1, { $subtract: ['$$message.postDate', ?0] }] }, { $subtract: [?1, ?0] }] }] } }] } } } } } } }",

            // Proietta il peso totale
            "{ $project: { game: 1, totalWeight: { $sum: '$messages.weight' } } }",

            // Raggruppa per gioco
            "{ $group: { _id: '$game._id', game: { $first: '$game' }, importanceIndex: { $sum: '$totalWeight' } } }",

            "{ $project: { _id: '$_id', name: '$game.name', yearReleased: '$game.yearReleased', importanceIndex: 1 } }",

            "{ $sort: { importanceIndex: -1 } }"
    })
    Slice<BestGameThreadDTO> getNormalizedGameRankingsWithDetails(Date startDate, Date endDate, Pageable pageable);


}

