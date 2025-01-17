package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Review.AddReviewDTO;
import com.example.BoardVerse.DTO.Review.ReviewGame;
import com.example.BoardVerse.DTO.Review.ReviewInfoDTO;
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

    public void addReview(String username, String gameId, AddReviewDTO addReviewDTO) {
        // Verifica se il gioco esiste
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        // Verifica se l'utente esiste
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        // Crea la recensione
        Review review = new Review();
        review.setAuthorUsername(username);
        review.setAuthorLocation(user.getLocation());
        review.setGameId(gameId);
        review.setComment(addReviewDTO.getComment());
        review.setRating(addReviewDTO.getRating());
        review.setCreatedAt(new Date()); // Imposta la data corrente

        // Salva la recensione nella collection Review
        Review savedReview;
        try {
            savedReview = reviewRepository.save(review);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the review: " + e.getMessage(), e);
        }

        ReviewGame reviewGame = ReviewMapper.toGame(savedReview);
        ReviewUser reviewUser = ReviewMapper.toUser(savedReview);

        // Aggiungi la recensione al gioco
        try {
            addReviewGame(gameId, reviewGame);
        } catch (Exception e) {
            // Rollback: elimina la recensione salvata
            reviewRepository.deleteById(savedReview.getId());
            throw new IllegalStateException("Error adding review to the game: " + e.getMessage(), e);
        }

        // Aggiungi la recensione all'utente
        try {
            addReviewUser(username, reviewUser);
        } catch (Exception e) {
            // Rollback: elimina la recensione e rimuovila dal gioco
            reviewRepository.deleteById(savedReview.getId());
            removeReviewGame(gameId, savedReview);
            throw new IllegalStateException("Error adding review to the user: " + e.getMessage(), e);
        }
    }

    public void addReviewUser(String username, ReviewUser review) {
        // Trova l'utente nel database
        User userMongo = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // Ottieni la lista delle recensioni recenti
        List<ReviewUser> list = userMongo.getMostRecentReviews();
        // Aggiungi la nuova recensione in cima alla lista
        list.add(0, review);
        // Limita la lista alla dimensione massima definita da Constants.RECENT_SIZE
        if (list.size() > Constants.RECENT_SIZE) {
            list.remove(list.size() - 1);
        }

        userMongo.setMostRecentReviews(list);
        // Salva l'utente aggiornato nel database
        userRepository.save(userMongo);
    }

    public void addReviewGame(String gameId, ReviewGame review){
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + gameId));

        // Verifica se il campo rating della recensione non è vuoto (null)
        if (review.rating() != null) {
            // Incrementa numRatings
            game.setNumRatings(game.getNumRatings() + 1);
        }

        List<ReviewGame> list = game.get().getMostRecentReviews();
        //aggiunge review in cima alla lista
        list.add(0, review);

        //rimuove l'ultima review se la lista è troppo lunga
        if(list.size() > Constants.RECENT_SIZE)
            list.remove(list.size() - 1);

        game.get().setMostRecentReviews(list);
        gameRepository.save(game.get());
    }

    //rimuovi review da Game
    public void removeReviewGame(String gameId, Review review){
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + gameId));

        //rimuovere il rating dal game
        if (review.getRating() != null) {
            // decrementa numRatings
            game.setNumRatings(game.getNumRatings() - 1);
        }

        List<ReviewGame> list = game.get().getMostRecentReviews();
        //rimuove review dalla lista
        list.removeIf(reviewGame -> reviewGame.id().equals(review.getId()));

        game.get().setMostRecentReviews(list);
        gameRepository.save(game.get());
    }


    public void deleteReview(String reviewId) {
        // Verifica se la recensione esiste
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // Elimina la recensione dalle review
        reviewRepository.delete(review);

        removeReviewGame(review.getGameId(), review);

        //rimuove la review dall'utente
        User user = userRepository.findByUsername(review.getAuthorUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + review.getAuthorUsername()));
        List<ReviewUser> list= user.get().getMostRecentReviews();
        //rimuove review dalla lista
        list.removeIf(reviewUser -> reviewUser.id().equals(reviewId));
        user.get().setMostRecentReviews(list);
        userRepository.save(user.get());

        //aggiornare la lista di mostRecentReviews dell'utente
        //aggiornare la lista di mostRecentReviews del gioco

    }

    public List<ReviewInfoDTO> findReviewByGameId(String gameId) {
        // Verifica se il gioco esiste
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: "+ gameId));
        // Trova tutte le recensioni di un gioco
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
