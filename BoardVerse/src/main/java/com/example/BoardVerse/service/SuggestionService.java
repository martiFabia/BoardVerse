package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Game.GameSuggestionDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentSuggestionDTO;
import com.example.BoardVerse.DTO.User.UserFollowRecommendationDTO;
import com.example.BoardVerse.DTO.User.UserTastesSuggestionDTO;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.repository.UserNeo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for providing suggestions to users.
 */
@Service
public class SuggestionService {

    private final UserNeo4jRepository userNeo4jRepository;

    /**
     * Constructor for SuggestionService.
     *
     * @param userNeo4jRepository the user repository
     */
    @Autowired
    public SuggestionService(UserNeo4jRepository userNeo4jRepository) {
        this.userNeo4jRepository = userNeo4jRepository;
    }

    /**
     * Retrieves a list of suggested users to follow based on the given username.
     *
     * @param username the username to base suggestions on
     * @return a list of user follow recommendation DTOs
     * @throws NotFoundException if the user is not found
     */
    public List<UserFollowRecommendationDTO> getSuggestedUsers(String username, int pageSize, int pageNumber) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getUsersRecommendationBySimilarNetwork(username, pageSize, pageNumber);
    }

    /**
     * Retrieves a list of users with similar tastes based on the given username.
     *
     * @param username the username to base suggestions on
     * @return a list of user tastes suggestion DTOs
     * @throws NotFoundException if the user is not found
     */
    public List<UserTastesSuggestionDTO> getSimilarUsers(String username, int pageSize, int pageNumber) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getUsersRecommendationBySimilarTastes(username, pageSize, pageNumber);
    }

    /**
     * Retrieves a list of suggested tournaments based on the given username.
     *
     * @param username the username to base suggestions on
     * @return a list of tournament suggestion DTOs
     * @throws NotFoundException if the user is not found
     */
    public List<TournamentSuggestionDTO> getSuggestedTournaments(String username, int pageSize, int pageNumber) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getTournamentsRecommendation(username, pageSize, pageNumber);
    }

    /**
     * Retrieves a list of suggested games based on the given username.
     *
     * @param username the username to base suggestions on
     * @return a list of game suggestion DTOs
     * @throws IllegalArgumentException if the user is not found
     */
    public List<GameSuggestionDTO> getSuggestedGames(String username, int pageSize, int pageNumber) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        return userNeo4jRepository.getGamesRecommendation(username, pageSize, pageNumber);
    }
}