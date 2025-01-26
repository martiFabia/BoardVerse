package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Review.AddReviewDTO;
import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.model.MongoDB.subentities.ReviewUser;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.Review;
import com.example.BoardVerse.model.MongoDB.User;
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
    public void addReview(String userId, String gameId, AddReviewDTO addReviewDTO) {

        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + userId));

        // Create the review
        Review review = new Review();
        review.setAuthorUsername(user.getUsername());
        review.setAuthorBirthDate(user.getBirthDate());
        review.setLocation(user.getLocation());
        review.setGame(new GameReview(game.getId(), game.getName(), game.getYearReleased(), game.getShortDescription()));
        review.setContent(addReviewDTO.getComment());
        review.setRating(addReviewDTO.getRating());
        review.setPostDate(new Date()); // Set the current date

        // Save the review in the Review collection
        try {
            reviewRepository.save(review);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the review: " + e.getMessage(), e);
        }

        ReviewUser reviewUser = ReviewMapper.toUser(review);

        // Add the rating to the game if present
        try {
            // Check if the review's rating field is not empty (null)
            if (review.getRating() != null) {
                addRating(game, review.getRating());
            }
        } catch (Exception e) {
            // Rollback: delete the saved review
            reviewRepository.deleteById(review.getId());
            throw new IllegalStateException("Error adding review to the game: " + e.getMessage(), e);
        }

        // Add the review to the user
        try {
            addReviewUser(user, reviewUser);
        } catch (Exception e) {
            // Rollback: delete the review and remove it from the game
            reviewRepository.deleteById(review.getId());
            removeRating(game, review.getRating());
            throw new IllegalStateException("Error adding review to the user: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a review to the user's most recent reviews.
     *
     * @param user the user
     * @param review the review
     */
    public void addReviewUser(User user, ReviewUser review) {
        // Get the list of recent reviews
        List<ReviewUser> list = user.getMostRecentReviews();
        // Add the new review to the top of the list
        list.add(0, review);
        // Limit the list to the maximum size defined by Constants.RECENT_SIZE
        if (list.size() > Constants.RECENT_SIZE) {
            list.remove(list.size() - 1);
        }

        user.setMostRecentReviews(list);
        // Save the updated user in the database
        userRepository.save(user);
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
    public void deleteReview(String reviewId, String gameId, String username) {
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        User user = userRepository.findByUsername(review.getAuthorUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + review.getAuthorUsername()));

        // Check if the user is the author of the review or an admin
        if(!username.equals(review.getAuthorUsername()) || !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("You can delete only your reviews");
        }
        // Delete the review from reviews
        reviewRepository.delete(review);

        // Remove the rating from the game
        if(review.getRating() != null) {
            removeRating(game, review.getRating());
        }

        // Remove the review from the user
        List<ReviewUser> list= user.getMostRecentReviews();
        // Remove review from the list
        list.removeIf(reviewUser -> reviewUser.id().equals(reviewId));

        // If the list has less than 3 items, add the next most recent review
        if (list.size() < Constants.RECENT_SIZE) {
            Review nextRecentReviewForUser = reviewRepository.findFirstByAuthorUsernameOrderByPostDateDesc(review.getAuthorUsername())
                    .orElse(null);
            if (nextRecentReviewForUser != null) {
                ReviewUser mappnextRecentReviewUser =ReviewMapper.toUser(nextRecentReviewForUser);
                list.add(mappnextRecentReviewUser);
            }
        }
        user.setMostRecentReviews(list);
        userRepository.save(user);

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
    public void updateReview(String gameId, String username, String reviewId, AddReviewDTO addReviewDTO) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // Check if the user is the author of the review
        if(!username.equals(review.getAuthorUsername())) {
            throw new IllegalArgumentException("You can update only your reviews");
        }

        // Update the review
        if(addReviewDTO.getComment() != null){
            review.setContent(addReviewDTO.getComment());
        }
        if(addReviewDTO.getRating() != null){
            GameMongo game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
            // Remove the old rating from the game
            removeRating(game, review.getRating());
            // Add the new rating to the game
            addRating(game, addReviewDTO.getRating());
            // Update the review's rating
            review.setRating(addReviewDTO.getRating());
        }

        reviewRepository.save(review);
    }
}