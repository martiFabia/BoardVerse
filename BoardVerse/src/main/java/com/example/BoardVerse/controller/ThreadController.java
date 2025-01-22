package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Thread.MessageCreationDTO;
import com.example.BoardVerse.DTO.Thread.ThreadCreationDTO;
import com.example.BoardVerse.DTO.Thread.ThreadPreviewDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.ThreadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/thread")
@Tag(name = "Thread", description = "Operations related to threads")
public class ThreadController {
    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    /* ================================ THREAD CRUD ================================ */


    //aggiungi thread
    @PostMapping("/{gameId}/{gameName}/{gameYearReleased}/thread/add")
        public ResponseEntity<String> addThread (@PathVariable String gameId, @PathVariable String gameName,
        @PathVariable int gameYearReleased, @Valid @RequestBody ThreadCreationDTO addThreadDTO)
        {
            try {
                UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                threadService.addThread(user.getUser(), gameId, gameName, gameYearReleased, addThreadDTO);
                return ResponseEntity.ok("Thread successfully added!");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
            }

        }

    //elimina thread
    @DeleteMapping("/{threadId}")
    public ResponseEntity<String> deleteThread(@PathVariable String threadId) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.deleteThread(user.getUser(),threadId);
            return ResponseEntity.ok("Thread successfully deleted!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //aggiungi un messaggio
    @PatchMapping("/{threadId}/messages/add")
    public ResponseEntity<String> addMessage(@PathVariable String threadId, @Valid @RequestBody MessageCreationDTO newMessageDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.addMessage(user.getUser(), threadId, newMessageDTO);
            return ResponseEntity.ok("Message successfully added!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //rispondi a un messaggio
    @PatchMapping("/{threadId}/messages/{messageId}/reply")
    public ResponseEntity<String> replyToMessage(
            @PathVariable String threadId,
            @PathVariable String messageId,
            @Valid @RequestBody MessageCreationDTO replyDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.replyToMessage(user.getUser(), threadId, messageId, replyDTO);
            return ResponseEntity.ok("Reply successfully added to message!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PatchMapping("/{threadId}/messages/delete")
    public ResponseEntity<String> deleteMessage(@PathVariable String threadId, @Valid @RequestBody String messageId) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.deleteMessage(user.getUser(), threadId, messageId);
            return ResponseEntity.ok("Message successfully deleted!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Slice<ThreadPreviewDTO>> filterThreads(
            @RequestParam(required = false) String gameName,
            @RequestParam(required = false) Integer yearReleased,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "lastPostDate") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(threadService.getFilteredThreads(
                gameName,yearReleased,startDate,endDate,tag, sortBy, order, page));
    }

}
