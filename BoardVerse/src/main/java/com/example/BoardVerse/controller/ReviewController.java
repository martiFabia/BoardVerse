package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Game.AddReviewDTO;
import com.example.BoardVerse.DTO.Game.ReviewInfoDTO;
import com.example.BoardVerse.model.MongoDB.Review;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@Tag(name = "Review", description = "Operations related to reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /* ================================ REVIEW CRUD ================================ */

    @PostMapping("/{gameId}/add")
    public ResponseEntity<String> addReview(@PathVariable String gameId, @Valid @RequestBody AddReviewDTO addReviewDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reviewService.addReview(user.getUsername(), gameId, addReviewDTO);
            return ResponseEntity.ok("Review successfully added!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    @DeleteMapping("/{reviewId}/delete")
    public ResponseEntity<String> deleteReview(@PathVariable String reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok("Review successfully deleted!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    @GetMapping("/{gameId}/find")
    public ResponseEntity<?> findReviewByGameId(@PathVariable String gameId) {
        try {
            List<ReviewInfoDTO> reviews = reviewService.findReviewByGameId(gameId);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    @PatchMapping("/{reviewId}/update")
    public ResponseEntity<String> updateReview(@PathVariable String reviewId, @Valid @RequestBody AddReviewDTO addReviewDTO) {
        try {
            reviewService.updateReview(reviewId, addReviewDTO);
            return ResponseEntity.ok("Review successfully updated!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }



}
