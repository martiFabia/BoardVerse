package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Review.AddReviewDTO;
import com.example.BoardVerse.DTO.Review.ReviewInfo;
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
@RequestMapping("/api/users")
@Tag(name = "Review", description = "Operations related to reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /* ================================ REVIEW CRUD ================================ */

    //aggiungi review
    @PostMapping("/{gameId}/{gameName}/{gameYearReleased}/review/add")
    public ResponseEntity<String> addReview(@PathVariable String gameId, @PathVariable String gameName, @PathVariable int gameYearReleased, @Valid @RequestBody AddReviewDTO addReviewDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reviewService.addReview(user.getUser(), gameId, gameName, gameYearReleased, addReviewDTO);
            return ResponseEntity.ok("Review successfully added!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //elimina review
    @DeleteMapping("/{reviewId}")
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

    @GetMapping("/{gameId}/{gameName}/{gameYearReleased}/review")
    public ResponseEntity<?> getReview(@PathVariable String gameId, @PathVariable String gameName, @PathVariable int gameYearReleased) {
        try {
            List<ReviewInfo> reviews = reviewService.getGameReview(gameId);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //aggiorna review
    @PatchMapping("/{gameId}/{gameName}/{gameYearReleased}/review/{reviewId}/update")
    public ResponseEntity<String> updateReview(@PathVariable String gameId, @PathVariable String gameName, @PathVariable int gameYearReleased, @PathVariable String reviewId, @Valid @RequestBody AddReviewDTO addReviewDTO) {
        try {
            reviewService.updateReview(gameId, reviewId, addReviewDTO);
            return ResponseEntity.ok("Review successfully updated!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }


}
