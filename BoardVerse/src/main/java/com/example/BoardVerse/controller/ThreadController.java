package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Thread.MessageCreationDTO;
import com.example.BoardVerse.DTO.Thread.ThreadCreationDTO;
import com.example.BoardVerse.DTO.Thread.ThreadInfoDTO;
import com.example.BoardVerse.DTO.Thread.ThreadPreviewDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.ThreadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{gameId}/thread/add")
        public ResponseEntity<String> addThread (@PathVariable String gameId, @Valid @RequestBody ThreadCreationDTO addThreadDTO)
        {
            try {
                UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                threadService.addThread(user.getUsername(), gameId, addThreadDTO);
                return ResponseEntity.ok("Thread successfully added!");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
            }

        }

    //elimina thread
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{threadId}")
    public ResponseEntity<String> deleteThread(@PathVariable String threadId) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.deleteThread(user.getUsername(), user.getUser().getRole(), threadId);
            return ResponseEntity.ok("Thread successfully deleted!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //aggiungi un messaggio
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{threadId}/messages/add")
    public ResponseEntity<String> addMessage(@PathVariable String threadId, @Valid @RequestBody MessageCreationDTO newMessageDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.addMessage(user.getUsername(), threadId, newMessageDTO);
            return ResponseEntity.ok("Message successfully added!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //rispondi a un messaggio
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{threadId}/messages/{messageId}/reply")
    public ResponseEntity<String> replyToMessage(
            @PathVariable String threadId,
            @PathVariable String messageId,
            @Valid @RequestBody MessageCreationDTO replyDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.replyToMessage(user.getUsername(), threadId, messageId, replyDTO);
            return ResponseEntity.ok("Reply successfully added to message!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{threadId}/messages/{messageId}/edit")
    public ResponseEntity<String> editMessage(@PathVariable String threadId, @PathVariable String messageId, @Valid @RequestBody MessageCreationDTO editDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.editMessage(user.getUsername(), threadId, messageId, editDTO);
            return ResponseEntity.ok("Message successfully edited!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{threadId}/messages/{messageId}/delete")
    public ResponseEntity<String> deleteMessage(@PathVariable String threadId, @PathVariable String messageId) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            threadService.deleteMessage(user.getUsername(), user.getUser().getRole(), threadId, messageId);
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startPostDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endPostDate,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "lastPostDate") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(threadService.getFilteredThreads(
                gameName,yearReleased,startPostDate,endPostDate,tag, sortBy, order, page));
    }

    //restituisce il thread
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{threadId}/thread")
    public ResponseEntity<?> getThread(@PathVariable String threadId) {
        try {
            return ResponseEntity.ok(threadService.getThread(threadId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

}
