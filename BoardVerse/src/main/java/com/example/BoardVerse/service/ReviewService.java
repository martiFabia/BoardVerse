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

@Service
public class ReviewService {

    @Autowired
    private UserMongoRepository userRepository;
    private final GameMongoRepository gameRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(UserMongoRepository userRepository, GameMongoRepository gameRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.reviewRepository = reviewRepository;
    }

    public void addReview(String userId, String gameId, AddReviewDTO addReviewDTO) {

        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + userId));

        // Crea la recensione
        Review review = new Review();
        review.setAuthorUsername(user.getUsername());
        review.setAuthorBirthDate(user.getBirthDate());
        review.setLocation(user.getLocation());
        review.setGame(new GameReview(game.getId(), game.getName(), game.getYearReleased(), game.getShortDescription()));
        review.setContent(addReviewDTO.getComment());
        review.setRating(addReviewDTO.getRating());
        review.setPostDate(new Date()); // Imposta la data corrente

        // Salva la recensione nella collection Review
        try {
            reviewRepository.save(review);
        } catch (Exception e) {
            throw new IllegalStateException("Error saving the review: " + e.getMessage(), e);
        }

        ReviewUser reviewUser = ReviewMapper.toUser(review);

        // Aggiungi il rating al gioco se è presente
        try {
            // Verifica se il campo rating della recensione non è vuoto (null)
            if (review.getRating() != null) {
                addRating(game, review.getRating());
            }
        } catch (Exception e) {
            // Rollback: elimina la recensione salvata
            reviewRepository.deleteById(review.getId());
            throw new IllegalStateException("Error adding review to the game: " + e.getMessage(), e);
        }

        // Aggiungi la recensione all'utente
        try {
            addReviewUser(user, reviewUser);
        } catch (Exception e) {
            // Rollback: elimina la recensione e rimuovila dal gioco
            reviewRepository.deleteById(review.getId());
            removeRating(game, review.getRating());
            throw new IllegalStateException("Error adding review to the user: " + e.getMessage(), e);
        }

        //AGGIUNGERE RAMO AL GRAPH (FORSE)
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
        double totalRating = game.getAverageRating() * game.getRatingVoters();
        // Aggiungi il nuovo rating
        totalRating += rating;
        // Incrementa numRatings
        game.setRatingVoters(game.getRatingVoters() + 1);
        // Calcola la nuova media
        game.setAverageRating(totalRating / game.getRatingVoters());
        gameRepository.save(game);
    }

    //rimuovi rating da Game
    public void removeRating(GameMongo game, Double rating){
        //rimuovere il rating dal game
        if (rating != null) {
            // moltiplica averageRating per numRatings
            double totalRating = game.getAverageRating() * game.getRatingVoters();
            // Rimuovi il rating
            totalRating -= rating;
            // decrementa numRatings
            game.setRatingVoters(game.getRatingVoters() - 1);
            // Calcola la nuova media
            game.setAverageRating(totalRating / game.getRatingVoters());
        }

        gameRepository.save(game);
    }


    public void deleteReview(String reviewId, String gameId, String username) {
        GameMongo game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        User user = userRepository.findByUsername(review.getAuthorUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + review.getAuthorUsername()));

        // Verifica se l'utente è l'autore della recensione o un admin
        if(!username.equals(review.getAuthorUsername()) || !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("You can delete only your reviews");
        }
        // Elimina la recensione dalle review
        reviewRepository.delete(review);

        //rimuovere il rating dal game
        if(review.getRating() != null) {
            removeRating(game, review.getRating());
        }

        //rimuovo la review dall'utente
        List<ReviewUser> list= user.getMostRecentReviews();
        //rimuove review dalla lista
        list.removeIf(reviewUser -> reviewUser.id().equals(reviewId));

        // Se la lista ha meno di 3 elementi, aggiungi la prossima recensione più recente
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

        //ELIMINARE RAMO DAL GRAPH

    }

    public Slice<ReviewInfo> getGameReviews(String gameId,String sortBy, int page) {
        //Determina su quale campo ordinare
        Sort sort;
        if ("rating".equalsIgnoreCase(sortBy)) {
            // Se vuoi considerare asc/desc
            sort = Sort.by("rating").descending();
        } else {
            // di default ordiniamo su postDate
            sort = Sort.by("postDate").descending();
        }

        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE, sort);
        return reviewRepository.findByGameId(gameId, pageable);

    }

    public void updateReview(String gameId, String username, String reviewId, AddReviewDTO addReviewDTO) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // Verifica se l'utente è l'autore della recensione
        if(!username.equals(review.getAuthorUsername())) {
            throw new IllegalArgumentException("You can update only your reviews");
        }

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

