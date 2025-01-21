package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Review.AddReviewDTO;
import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game/")
@Tag(name = "Review", description = "Operations related to reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /* ================================ REVIEW CRUD ================================ */

    //aggiungi review
    @PostMapping("/{gameId}/review")
    public ResponseEntity<String> addReview(@PathVariable String gameId, @Valid @RequestBody AddReviewDTO addReviewDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reviewService.addReview(user.getUser(), gameId, addReviewDTO);
            return ResponseEntity.ok("Review successfully added!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //elimina review
    @DeleteMapping("/{gameId}/review/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable String gameId, @PathVariable String reviewId) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reviewService.deleteReview(reviewId, gameId, user.getUsername());
            return ResponseEntity.ok("Review successfully deleted!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //restituisce review del gioco
    @GetMapping("/{gameId}/review")
    public ResponseEntity<?> getReview(@PathVariable String gameId,
                                       @RequestParam(defaultValue = "postDate") String sortBy, @RequestParam(defaultValue = "0") int page) {
        try {
            Slice<ReviewInfo> reviews = reviewService.getGameReviews(gameId,sortBy, page);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //aggiorna review
    @PatchMapping("/{gameId}/review/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable String gameId, @PathVariable String reviewId, @Valid @RequestBody AddReviewDTO addReviewDTO) {
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
