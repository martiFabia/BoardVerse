package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Review.AddReviewDTO;
import com.example.BoardVerse.DTO.Review.ReviewInfo;
import com.example.BoardVerse.DTO.Review.ReviewUser;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.Review;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.repository.GameMongoRepository;
import com.example.BoardVerse.repository.ReviewRepository;
import com.example.BoardVerse.repository.UserRepository;
import com.example.BoardVerse.utils.Constants;
import com.example.BoardVerse.utils.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private UserRepository userRepository;
    private final GameMongoRepository gameRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(UserRepository userRepository, GameMongoRepository gameRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.reviewRepository = reviewRepository;
    }

    public void addReview(User user, String gameId, String gameName, int gameYearReleased, AddReviewDTO addReviewDTO) {
        // Verifica se il gioco esiste
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        // Crea la recensione
        Review review = new Review();
        review.setAuthorUsername(user.getUsername());
        review.setAuthorLocation(user.getLocation());
        review.setGameId(gameId);
        review.setGameName(gameName);
        review.setGameYearReleased(gameYearReleased);
        review.setContent(addReviewDTO.getComment());
        review.setRating(addReviewDTO.getRating());
        review.setCreatedAt(new Date()); // Imposta la data corrente

        // Salva la recensione nella collection Review
        Review savedReview;
        try {
            savedReview = reviewRepository.save(review);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the review: " + e.getMessage(), e);
        }

        ReviewInfo reviewGame = ReviewMapper.toGame(savedReview);
        ReviewUser reviewUser = ReviewMapper.toUser(savedReview);

        // Aggiungi la recensione al gioco
        try {
            // Verifica se il campo rating della recensione non è vuoto (null)
            if (reviewGame.rating() != null) {
                addRating(game, reviewGame.rating());
            }


        } catch (Exception e) {
            // Rollback: elimina la recensione salvata
            reviewRepository.deleteById(savedReview.getId());
            throw new IllegalStateException("Error adding review to the game: " + e.getMessage(), e);
        }

        // Aggiungi la recensione all'utente
        try {
            addReviewUser(user, reviewUser);
        } catch (Exception e) {
            // Rollback: elimina la recensione e rimuovila dal gioco
            reviewRepository.deleteById(savedReview.getId());
            removeRating(game, savedReview.getRating());
            throw new IllegalStateException("Error adding review to the user: " + e.getMessage(), e);
        }
    }

    public void addReviewUser(User user, ReviewUser review) {
        // Ottieni la lista delle recensioni recenti
        List<ReviewUser> list = user.getMostRecentReviews();
        // Aggiungi la nuova recensione in cima alla lista
        list.add(0, review);
        // Limita la lista alla dimensione massima definita da Constants.RECENT_SIZE
        if (list.size() > Constants.RECENT_SIZE) {
            list.remove(list.size() - 1);
        }

        user.setMostRecentReviews(list);
        // Salva l'utente aggiornato nel database
        userRepository.save(user);
    }

    public void addRating(GameMongo game, Double rating){
        //moltiplica averageRating per numRatings
        double totalRating = game.getAverageRating() * game.getNumRatings();
        // Aggiungi il nuovo rating
        totalRating += rating;
        // Incrementa numRatings
        game.setNumRatings(game.getNumRatings() + 1);
        // Calcola la nuova media
        game.setAverageRating(totalRating / game.getNumRatings());
        gameRepository.save(game);
    }

    //rimuovi review da Game
    public void removeRating(GameMongo game, Double rating){
        //rimuovere il rating dal game
        if (rating != null) {
            // moltiplica averageRating per numRatings
            double totalRating = game.getAverageRating() * game.getNumRatings();
            // Rimuovi il rating
            totalRating -= rating;
            // decrementa numRatings
            game.setNumRatings(game.getNumRatings() - 1);
            // Calcola la nuova media
            game.setAverageRating(totalRating / game.getNumRatings());
        }

        gameRepository.save(game);
    }


    public void deleteReview(String reviewId) {
        // Verifica se la recensione esiste
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // Elimina la recensione dalle review
        reviewRepository.delete(review);

        //rimuovere il rating dal game
        if(review.getRating() != null) {
            //rimuovo rating
            GameMongo game = gameRepository.findById(review.getGameId())
                    .orElseThrow(() -> new NotFoundException("Game not found with ID: " + review.getGameId()));
            removeRating(game, review.getRating());
        }

        //rimuove la review dall'utente
        User user = userRepository.findByUsername(review.getAuthorUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + review.getAuthorUsername()));
        List<ReviewUser> list= user.getMostRecentReviews();
        //rimuove review dalla lista
        list.removeIf(reviewUser -> reviewUser.id().equals(reviewId));

        // Se la lista ha meno di 5 elementi, aggiungi la prossima recensione più recente
        if (list.size() < Constants.RECENT_SIZE) {
            Review nextRecentReviewForUser = reviewRepository.findFirstByAuthorUsernameOrderByCreatedAtDesc(review.getAuthorUsername())
                    .orElse(null);
            if (nextRecentReviewForUser != null) {
                ReviewUser mappnextRecentReviewUser =ReviewMapper.toUser(nextRecentReviewForUser);
                list.add(mappnextRecentReviewUser);
            }
        }
        user.setMostRecentReviews(list);
        userRepository.save(user);

    }

    public List<ReviewInfo> getGameReview(String gameId) {
        // Verifica se il gioco esiste
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: "+ gameId));
        // Trova tutte le recensioni di un gioco
        return reviewRepository.findByGameId(gameId).stream()
                .map(elem -> new ReviewInfo(elem.getId(), elem.getAuthorUsername(), elem.getRating(), elem.getContent(), elem.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void updateReview(String gameId, String reviewId, AddReviewDTO addReviewDTO) {
        // Verifica se la recensione esiste
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // Aggiorna la recensione
        if(addReviewDTO.getComment() != null){
            review.setContent(addReviewDTO.getComment());
        }
        if(addReviewDTO.getRating() != null){
            GameMongo game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
            //rimuovere il rating vecchio dal game
            removeRating(game, review.getRating());
            //aggiungere il nuovo rating al game
            addRating(game, addReviewDTO.getRating());
            //aggiornare il rating della review
            review.setRating(addReviewDTO.getRating());
        }

        reviewRepository.save(review);
    }
}

