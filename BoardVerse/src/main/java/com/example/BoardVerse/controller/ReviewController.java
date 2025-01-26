package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Review.AddReviewDTO;
import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/")
@Tag(name = "Review", description = "Operations related to reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /* ================================ REVIEW CRUD ================================ */

    @Operation(summary = "Add a new review")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{gameId}/reviews")
    public ResponseEntity<String> addReview(@PathVariable String gameId, @Valid @RequestBody AddReviewDTO addReviewDTO) {

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(reviewService.addReview(user.getId(), gameId, addReviewDTO));
    }

    @Operation(summary = "Delete a review")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{gameId}/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable String gameId, @PathVariable String reviewId) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(reviewService.deleteReview(reviewId, gameId, user.getUsername()));
    }

    @Operation(summary = "Get game reviews")
    @GetMapping("/{gameId}/reviews")
    public ResponseEntity<?> getReview(@PathVariable String gameId,
                                       @RequestParam(defaultValue = "postDate") String sortBy, @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(reviewService.getGameReviews(gameId,sortBy, page));

    }

    @Operation(summary = "Update a review")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{gameId}/reviews/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable String gameId, @PathVariable String reviewId, @Valid @RequestBody AddReviewDTO addReviewDTO) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(reviewService.updateReview(gameId, user.getUsername(), reviewId, addReviewDTO));
    }


}
