package com.example.BoardVerse.service;


import com.example.BoardVerse.DTO.Thread.MessageCreationDTO;
import com.example.BoardVerse.DTO.Thread.ThreadCreationDTO;
import com.example.BoardVerse.DTO.Thread.ThreadInfoDTO;
import com.example.BoardVerse.DTO.Thread.ThreadPreviewDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.ThreadMongo;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.model.MongoDB.subentities.GameThread;
import com.example.BoardVerse.model.MongoDB.subentities.Message;
import com.example.BoardVerse.model.MongoDB.subentities.ReplyTo;
import com.example.BoardVerse.model.MongoDB.subentities.Role;
import com.example.BoardVerse.repository.GameMongoRepository;
import com.example.BoardVerse.repository.ThreadRepository;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.substring;

@Service
public class ThreadService {

    @Autowired
    private final ThreadRepository threadRepository;
    private final UserMongoRepository userMongoRepository;
    private final GameMongoRepository gameRepository;

    public ThreadService(ThreadRepository threadRepository, UserMongoRepository userMongoRepository, GameMongoRepository gameRepository) {
        this.threadRepository = threadRepository;
        this.userMongoRepository = userMongoRepository;
        this.gameRepository = gameRepository;
    }

    public void addThread(String username, String gameId, ThreadCreationDTO addThreadDTO) {
        // Verifica se il gioco esiste
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        String threadId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();

        // Crea il thread
        ThreadMongo thread = new ThreadMongo();
        thread.setAuthorUsername(username);
        thread.setPostDate(new Date()); // Imposta la data corrente
        thread.setContent(addThreadDTO.getSubjectContent());
        thread.setId(threadId);
        thread.setTag(addThreadDTO.getTag());
        thread.setGame(new GameThread(gameId, game.getName(), game.getYearReleased()));
        thread.setLastPostDate(new Date());
        thread.setMessages(new ArrayList<>());

        Message message = new Message();
        message.setId(messageId);
        message.setAuthorUsername(username);
        message.setPostDate(new Date());
        message.setContent(addThreadDTO.getMessageContent());
        thread.getMessages().add(message);

        // Salva il thread nella collection Thread
        try {
            threadRepository.save(thread);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the thread: " + e.getMessage(), e);
        }

    }


    public void deleteThread(String username, Role role, String threadId) {
        // Verifica se il thread esiste
        ThreadMongo thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found with ID: " + threadId));

        // Verifica se l'utente è l'autore del thread o è l'admin
        if(!Objects.equals(username, threadRepository.findById(threadId).get().getAuthorUsername())
                || role.equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("You are not the author of this thread and you are not an admin");
        }

        // Elimina il thread
        try {
            threadRepository.deleteById(threadId);
        } catch (Exception e) {
            throw new IllegalStateException("Error deleting the thread: " + e.getMessage(), e);
        }
    }

    public void addMessage(String username, String threadId, MessageCreationDTO newMessageDTO) {
        // Verifica se il thread esiste
        ThreadMongo thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found with ID: " + threadId));

        String messageId = UUID.randomUUID().toString();

        // Crea il messaggio
        Message newMessage = new Message();
        newMessage.setId(messageId);
        newMessage.setAuthorUsername(username);
        newMessage.setPostDate(new Date()); // Imposta la data corrente
        newMessage.setContent(newMessageDTO.getMessageContent());

        // Aggiungi il messaggio alla lista dei messaggi del thread
        thread.getMessages().add(newMessage);

        // Aggiorna la data dell'ultimo messaggio
        thread.setLastPostDate(new Date());

        // Salva il thread nella collection Thread
        try {
            threadRepository.save(thread);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the message: " + e.getMessage(), e);
        }
    }

    public void replyToMessage(String username, String threadId, String messageId, MessageCreationDTO replyDTO) {
        // Verifica se il thread esiste
        ThreadMongo thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found with ID: " + threadId));

        Message originalMessage = thread.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        String replyId = UUID.randomUUID().toString();

        // Crea il messaggio di risposta
        Message reply = new Message();
        reply.setId(replyId);
        reply.setAuthorUsername(username);
        reply.setPostDate(new Date()); // Imposta la data corrente
        reply.setContent(replyDTO.getMessageContent());


        reply.setReplyTo(new ReplyTo(
                originalMessage.getAuthorUsername(),
                originalMessage.getId(),
                originalMessage.getContent().substring(0, Math.min(originalMessage.getContent().length(), 50))
        ));

        thread.getMessages().add(reply);

        // Aggiorna la data dell'ultimo messaggio
        thread.setLastPostDate(new Date());

        // Salva il thread nella collection Thread
        try {
            threadRepository.save(thread);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the reply: " + e.getMessage(), e);
        }
    }


    public void editMessage(String username, String threadId, String messageId, MessageCreationDTO editDTO) {
        // Verifica se il thread esiste
        ThreadMongo thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found with ID: " + threadId));

        // Verifica se l'utente è l'autore del messaggio
        if(!Objects.equals(username, thread.getMessages().stream().filter(m -> m.getId().equals(messageId)).findFirst().get().getAuthorUsername())) {
            throw new IllegalArgumentException("You are not the author of this message");
        }

        // Modifica il messaggio
        thread.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .ifPresent(m -> m.setContent(editDTO.getMessageContent()));

        // Aggiorna i contentPreview dei messaggi che fanno riferimento al messaggio modificato
        thread.getMessages().stream()
                .filter(m -> m.getReplyTo() != null && messageId.equals(m.getReplyTo().getMessageUUID()))
                .forEach(m -> m.getReplyTo().setContentPreview(editDTO.getMessageContent().substring(0, Math.min(editDTO.getMessageContent().length(), 50))));

        // Salva il thread nella collection Thread
        try {
            threadRepository.save(thread);
        } catch (Exception e) {
            throw new IllegalStateException("Error editing the message: " + e.getMessage(), e);
        }
    }


    public void deleteMessage(String username, Role role,  String threadId, String messageId) {
        // Verifica se il thread esiste
        ThreadMongo thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found with ID: " + threadId));

        // Verifica se l'utente è l'autore del messaggio o è l'admin
        if(!Objects.equals(username, thread.getMessages().stream().filter(m -> m.getId().equals(messageId)).findFirst().get().getAuthorUsername())
                || !role.equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("You are not the author of this message and you are not an admin");
        }

        // Elimina il messaggio
        thread.getMessages().removeIf(m -> m.getId().equals(messageId));

// Aggiorna messageUUID e authorUsername dei messaggi che fanno riferimento al messaggio eliminato impostandoli a null
        thread.getMessages().stream()
                .filter(m -> m.getReplyTo() != null && messageId.equals(m.getReplyTo().getMessageUUID()))
                .forEach(m -> {
                    m.getReplyTo().setMessageUUID(null); // Imposta a null il messageUUID
                    m.getReplyTo().setUsername(null); // Imposta a null l'authorUsername
                });


        // Salva il thread nella collection Thread
        try {
            threadRepository.save(thread);
        } catch (Exception e) {
            throw new IllegalStateException("Error deleting the message: " + e.getMessage(), e);
        }
    }


    public Slice<ThreadPreviewDTO> getFilteredThreads(String gameName, Integer YearReleased, Date startPostDate, Date endPostDate, String tag, String sortBy, String order, int page) {

        Sort sort;
        if("creationDate".equalsIgnoreCase(sortBy)) {
            sort = "asc".equalsIgnoreCase(order)
                    ? Sort.by("postDate").ascending()
                    : Sort.by("postDate").descending();
        } else if ("messageCount".equalsIgnoreCase(sortBy)) {
            sort = "asc".equalsIgnoreCase(order)
                    ? Sort.by("messageCount").ascending()
                    : Sort.by("messageCount").descending();
        }
        else {
            sort = "asc".equalsIgnoreCase(order)
                    ? Sort.by("lastPostDate").ascending()
                    : Sort.by("lastPostDate").descending();
        }
        // Imposta un valore predefinito per gameName se è null
        if (gameName == null) {
            gameName = ""; // Valore di default per evitare errori con $regex
        }
        if(startPostDate == null){
            startPostDate = new Date(0);
        }
        if (endPostDate == null) {
            endPostDate = new Date();
        }

        return threadRepository.findFilteredThread(gameName, YearReleased, startPostDate, endPostDate, tag, PageRequest.of(page, Constants.PAGE_SIZE, sort));

    }

    public ThreadInfoDTO getThread(String threadId) {
        // Verifica se il thread esiste
        ThreadMongo thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found with ID: " + threadId));

        int messagesCount = thread.getMessages() != null ? thread.getMessages().size() : 0;

        return new ThreadInfoDTO(thread.getAuthorUsername(), thread.getPostDate(), thread.getContent(), thread.getTag(), thread.getGame(), thread.getLastPostDate(), thread.getMessages(), messagesCount);
    }


}
