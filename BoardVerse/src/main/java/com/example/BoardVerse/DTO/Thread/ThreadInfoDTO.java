package com.example.BoardVerse.DTO.Thread;

import com.example.BoardVerse.model.MongoDB.subentities.GameThread;
import com.example.BoardVerse.model.MongoDB.subentities.Message;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ThreadInfoDTO {
    private String authorUsername;
    private Date postDate;
    private String content;
    private String tag;
    private GameThread game;
    private Date lastPostDate;
    private List<Message> messages;
    private int countMessages;

    public ThreadInfoDTO(String authorUsername, Date postDate, String content, String tag, GameThread game, Date lastPostDate, List<Message> messages, int countMessages) {
        this.authorUsername = authorUsername;
        this.postDate = postDate;
        this.content = content;
        this.tag = tag;
        this.game = game;
        this.lastPostDate = lastPostDate;
        this.messages = messages;
        this.countMessages = countMessages;
    }

}
