package com.example.BoardVerse.model.MongoDB;

import com.example.BoardVerse.model.MongoDB.subentities.GameThread;
import com.example.BoardVerse.model.MongoDB.subentities.Message;
import com.example.BoardVerse.model.MongoDB.subentities.Post;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;


@Document(collection = "threads")
@Data
public class ThreadMongo extends Post {
    private String id;
    private String tag;
    private GameThread game;
    private Date lastPostDate;

    private List<Message> messages;
}
