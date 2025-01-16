package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Game.AddReviewDTO;
import com.example.BoardVerse.DTO.Game.ReviewInfoDTO;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.Game;
import com.example.BoardVerse.model.MongoDB.Review;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.repository.GameRepository;
import com.example.BoardVerse.repository.ReviewRepository;
import com.example.BoardVerse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private UserRepository userRepository;
    private final GameRepository gameRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(UserRepository userRepository, GameRepository gameRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.reviewRepository = reviewRepository;
    }


    public void addReview(String username, String gameId, AddReviewDTO addReviewDTO) {
        // Verifica se il gioco esiste
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: "+ gameId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // Crea e salva la recensione
        Review review = new Review();
        review.setAuthorUsername(username);
        review.setAuthorLocation(user.getLocation());
        review.setGameId(gameId);
        review.setComment(addReviewDTO.getComment());
        review.setRating(addReviewDTO.getRating());
        review.setCreatedAt(new Date()); // Aggiungi la data corrente

        //aggiungere il rating al game

        reviewRepository.save(review);
    }

    public void deleteReview(String reviewId) {
        // Verifica se la recensione esiste
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        //rimuovere il rating dal game

        // Elimina la recensione
        reviewRepository.delete(review);
    }

    public List<ReviewInfoDTO> findReviewByGameId(String gameId) {
        // Verifica se il gioco esiste
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: "+ gameId));

        // Trova tutte le recensioni di un gioco
        List<Review> reviewList= reviewRepository.findByGameId(gameId);

        return reviewRepository.findByGameId(gameId).stream()
                .map(elem -> new ReviewInfoDTO(elem.getAuthorUsername(), elem.getRating(), elem.getComment(), elem.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void updateReview(String reviewId, AddReviewDTO addReviewDTO) {
        // Verifica se la recensione esiste
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // Aggiorna la recensione
        if(addReviewDTO.getComment() != null){
            review.setComment(addReviewDTO.getComment());
        }
        if(addReviewDTO.getRating() != null){
            review.setRating(addReviewDTO.getRating());
            //rimuovere il rating dal game
            //aggiungere il nuovo rating al game
        }

        reviewRepository.save(review);
    }
}
