package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Review.AddReviewDTO;
import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.model.MongoDB.ReviewMongo;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.model.MongoDB.subentities.ReviewUser;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.subentities.GameReview;
import com.example.BoardVerse.model.MongoDB.subentities.Role;
import com.example.BoardVerse.repository.GameMongoRepository;
import com.example.BoardVerse.repository.ReviewRepository;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.utils.Constants;
import com.example.BoardVerse.utils.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Service class for managing reviews.
 */
@Service
public class ReviewService {

    @Autowired
    private UserMongoRepository userRepository;
    private final GameMongoRepository gameRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Constructor for ReviewService.
     *
     * @param userRepository the user repository
     * @param gameRepository the game repository
     * @param reviewRepository the review repository
     */
    public ReviewService(UserMongoRepository userRepository, GameMongoRepository gameRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Adds a new review.
     *
     * @param userId the user ID
     * @param gameId the game ID
     * @param addReviewDTO the review creation DTO
     */
    public String addReview(String userId, String gameId, AddReviewDTO addReviewDTO) {

        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        UserMongo userMongo = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + userId));

        // Create the reviewMongo
        ReviewMongo reviewMongo = new ReviewMongo();
        reviewMongo.setAuthorUsername(userMongo.getUsername());
        reviewMongo.setAuthorBirthDate(userMongo.getBirthDate());
        reviewMongo.setLocation(userMongo.getLocation());
        reviewMongo.setGame(new GameReview(game.getId(), game.getName(), game.getYearReleased(), game.getShortDescription()));
        reviewMongo.setContent(addReviewDTO.getComment());
        reviewMongo.setRating(addReviewDTO.getRating());
        reviewMongo.setPostDate(new Date()); // Set the current date

        // Save the reviewMongo in the ReviewMongo collection
        try {
            reviewRepository.save(reviewMongo);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the review: " + e.getMessage(), e);
        }

        ReviewUser reviewUser = ReviewMapper.toUser(reviewMongo);

        // Add the rating to the game if present
        try {
            // Check if the reviewMongo's rating field is not empty (null)
            if (reviewMongo.getRating() != null) {
                addRating(game, reviewMongo.getRating());
            }
        } catch (Exception e) {
            // Rollback: delete the saved reviewMongo
            reviewRepository.deleteById(reviewMongo.getId());
            throw new IllegalStateException("Error adding review to the game: " + e.getMessage(), e);
        }

        // Add the reviewMongo to the userMongo
        try {
            addReviewUser(userMongo, reviewUser);
        } catch (Exception e) {
            // Rollback: delete the reviewMongo and remove it from the game
            reviewRepository.deleteById(reviewMongo.getId());
            removeRating(game, reviewMongo.getRating());
            throw new IllegalStateException("Error adding review to the user: " + e.getMessage(), e);
        }

        return "Review successfully added!";
    }

    /**
     * Adds a review to the user's most recent reviews.
     *
     * @param userMongo the user
     * @param review the review
     */
    public void addReviewUser(UserMongo userMongo, ReviewUser review) {
        // Get the list of recent reviews
        List<ReviewUser> list = userMongo.getMostRecentReviews();
        // Add the new review to the top of the list
        list.add(0, review);
        // Limit the list to the maximum size defined by Constants.RECENT_SIZE
        if (list.size() > Constants.RECENT_SIZE) {
            list.remove(list.size() - 1);
        }

        userMongo.setMostRecentReviews(list);
        // Save the updated userMongo in the database
        userRepository.save(userMongo);
    }

    /**
     * Adds a rating to a game.
     *
     * @param game the game
     * @param rating the rating
     */
    public void addRating(GameMongo game, Double rating){
        // Multiply averageRating by numRatings
        double totalRating = game.getAverageRating() * game.getRatingVoters();
        // Add the new rating
        totalRating += rating;
        // Increment numRatings
        game.setRatingVoters(game.getRatingVoters() + 1);
        // Calculate the new average
        game.setAverageRating(totalRating / game.getRatingVoters());
        gameRepository.save(game);
    }

    /**
     * Removes a rating from a game.
     *
     * @param game the game
     * @param rating the rating
     */
    public void removeRating(GameMongo game, Double rating){
        // Remove the rating from the game
        if (rating != null) {
            // Multiply averageRating by numRatings
            double totalRating = game.getAverageRating() * game.getRatingVoters();
            // Remove the rating
            totalRating -= rating;
            // Decrement numRatings
            game.setRatingVoters(game.getRatingVoters() - 1);
            // Calculate the new average
            game.setAverageRating(totalRating / game.getRatingVoters());
        }

        gameRepository.save(game);
    }

    /**
     * Deletes a review.
     *
     * @param reviewId the review ID
     * @param gameId the game ID
     * @param username the username
     */
    public String deleteReview(String reviewId, String gameId, String username) {
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        ReviewMongo reviewMongo = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with ID: " + reviewId));

        UserMongo userMongo = userRepository.findByUsername(reviewMongo.getAuthorUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + reviewMongo.getAuthorUsername()));

        // Check if the userMongo is the author of the reviewMongo or an admin
        if(!username.equals(reviewMongo.getAuthorUsername()) || !userMongo.getRole().equals(Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("You can delete only your reviews");
        }
        // Delete the reviewMongo from reviews
        reviewRepository.delete(reviewMongo);

        // Remove the rating from the game
        if(reviewMongo.getRating() != null) {
            removeRating(game, reviewMongo.getRating());
        }

        // Remove the reviewMongo from the userMongo
        List<ReviewUser> list= userMongo.getMostRecentReviews();
        // Remove reviewMongo from the list
        list.removeIf(reviewUser -> reviewUser.id().equals(reviewId));

        // If the list has less than 3 items, add the next most recent reviewMongo
        if (list.size() < Constants.RECENT_SIZE) {
            ReviewMongo nextRecentReviewForUserMongo = reviewRepository.findFirstByAuthorUsernameOrderByPostDateDesc(reviewMongo.getAuthorUsername())
                    .orElse(null);
            if (nextRecentReviewForUserMongo != null) {
                ReviewUser mappnextRecentReviewUser =ReviewMapper.toUser(nextRecentReviewForUserMongo);
                list.add(mappnextRecentReviewUser);
            }
        }
        userMongo.setMostRecentReviews(list);
        userRepository.save(userMongo);

        return "Review successfully deleted!";
    }

    /**
     * Gets reviews for a game.
     *
     * @param gameId the game ID
     * @param sortBy the sort field
     * @param page the page number
     * @return a slice of review information
     */
    public Slice<ReviewInfo> getGameReviews(String gameId,String sortBy, int page) {
        // Determine the field to sort by
        Sort sort;
        if ("rating".equalsIgnoreCase(sortBy)) {
            // Consider asc/desc
            sort = Sort.by("rating").descending();
        } else {
            // Default to sorting by postDate
            sort = Sort.by("postDate").descending();
        }

        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE, sort);
        return reviewRepository.findByGameId(gameId, pageable);

    }

    /**
     * Updates a review.
     *
     * @param gameId the game ID
     * @param username the username
     * @param reviewId the review ID
     * @param addReviewDTO the review update DTO
     */
    public String updateReview(String gameId, String username, String reviewId, AddReviewDTO addReviewDTO) {
        ReviewMongo reviewMongo = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with ID: " + reviewId));

        // Check if the user is the author of the reviewMongo
        if(!username.equals(reviewMongo.getAuthorUsername())) {
            throw new AccessDeniedException("You can update only your reviews");
        }

        // Update the reviewMongo
        if(addReviewDTO.getComment() != null){
            reviewMongo.setContent(addReviewDTO.getComment());
        }
        if(addReviewDTO.getRating() != null){
            GameMongo game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
            // Remove the old rating from the game
            removeRating(game, reviewMongo.getRating());
            // Add the new rating to the game
            addRating(game, addReviewDTO.getRating());
            // Update the review's rating
            reviewMongo.setRating(addReviewDTO.getRating());
        }

        reviewRepository.save(reviewMongo);
        return "Review successfully updated!";
    }
}