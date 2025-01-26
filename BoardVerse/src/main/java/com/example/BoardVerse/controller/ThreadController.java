package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Thread.*;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.ThreadService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api")
@Tag(name = "Thread", description = "Operations related to threads")
public class ThreadController {
    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    /* ================================ THREAD CRUD ================================ */


    @Operation(summary = "Add a new thread")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/games/{gameId}/threads")
        public ResponseEntity<String> addThread (@PathVariable String gameId, @Valid @RequestBody ThreadCreationDTO addThreadDTO)
        {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok( threadService.addThread(user.getUsername(), gameId, addThreadDTO));

        }


    @Operation(summary = "Delete a thread")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("threads/{threadId}")
    public ResponseEntity<String> deleteThread(@PathVariable String threadId) {

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok( threadService.deleteThread(user.getUsername(), user.getUser().getRole(), threadId));
    }

    @Operation(summary = "Add a new message to a thread")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/threads/{threadId}/messages")
    public ResponseEntity<String> addMessage(@PathVariable String threadId, @Valid @RequestBody MessageCreationDTO newMessageDTO) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(threadService.addMessage(user.getUsername(), threadId, newMessageDTO));
    }

    @Operation(summary = "Reply to a message")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/threads/{threadId}/messages/{messageId}/reply")
    public ResponseEntity<String> replyToMessage(
            @PathVariable String threadId,
            @PathVariable String messageId,
            @Valid @RequestBody MessageCreationDTO replyDTO) {

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(threadService.replyToMessage(user.getUsername(), threadId, messageId, replyDTO));
    }

    @Operation(summary = "Edit a message")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/threads/{threadId}/messages/{messageId}/edit")
    public ResponseEntity<String> editMessage(@PathVariable String threadId, @PathVariable String messageId, @Valid @RequestBody MessageCreationDTO editDTO) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(threadService.editMessage(user.getUsername(), threadId, messageId, editDTO));
    }


    @Operation(summary = "Delete a message")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/threads/{threadId}/messages/{messageId}/delete")
    public ResponseEntity<String> deleteMessage(@PathVariable String threadId, @PathVariable String messageId) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(threadService.deleteMessage(user.getUsername(), user.getUser().getRole(), threadId, messageId));
    }

    @Operation(summary = "Find threads by filter")
    @GetMapping("/threads/filter")
    public ResponseEntity<?> filterThreads(
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

    @Operation(summary = "Get game threads")
    @GetMapping("/games/{gameId}/threads")
    public ResponseEntity<?> getThreadsByGame(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "lastPostDate") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(threadService.getThreadsByGame(gameId, sortBy, order, page));
    }


    @Operation(summary = "Get a thread")
    @GetMapping("/threads/{threadId}")
    public ResponseEntity<?> getThread(@PathVariable String threadId) {
        return ResponseEntity.ok(threadService.getThread(threadId));
    }

}
