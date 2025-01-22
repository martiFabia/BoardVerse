package com.example.BoardVerse.repository;

import com.example.BoardVerse.DTO.Thread.ThreadPreviewDTO;
import com.example.BoardVerse.model.MongoDB.ThreadMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;

public interface ThreadRepository extends MongoRepository<ThreadMongo, String> {

    @Aggregation(pipeline = {
            "{ $match: { $and: [ "
                    + "  { $or: [ { $expr: { $eq: [ :#{#gameName}, null ] } }, { 'game.name': { $regex: :#{#gameName}, $options: 'i' } } ] }, "
                    + "  { $or: [ { $expr: { $eq: [ :#{#yearReleased}, null ] } }, { 'game.yearReleased': :#{#yearReleased} } ] }, "
                    + "  { $or: [ { $expr: { $eq: [ :#{#startDate}, null ] } }, { 'postDate': { $gte: :#{#startDate} } } ] }, "
                    + "  { $or: [ { $expr: { $eq: [ :#{#endDate}, null ] } }, { 'postDate': { $lte: :#{#endDate} } } ] }, "
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
            Date startDate,
            Date endDate,
            String tag,
            Pageable pageable);



}

