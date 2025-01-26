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

@Service
public class SuggestionService {

    private final UserNeo4jRepository userNeo4jRepository;

    @Autowired
    public SuggestionService(UserNeo4jRepository userNeo4jRepository) {
        this.userNeo4jRepository = userNeo4jRepository;
    }

    public List<UserFollowRecommendationDTO> getSuggestedUsers(String username) {
       userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("UserMongo not found with username: " + username));

        return userNeo4jRepository.getUsersRecommendationBySimilarNetwork(username, 5, 0);
    }

    public List<UserTastesSuggestionDTO>  getSimilarUsers(String username) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("UserMongo not found with username: " + username));

        return userNeo4jRepository.getUsersRecommendationBySimilarTastes(username, 5, 0);
    }

    public List<TournamentSuggestionDTO> getSuggestedTournaments(String username) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("UserMongo not found with username: " + username));

        return userNeo4jRepository.getTournamentsRecommendation(username, 5, 0);
    }

    public List<GameSuggestionDTO> getSuggestedGames(String username) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("UserMongo not found with username: " + username));

        return userNeo4jRepository.getGamesRecommendation(username, 5, 0);
    }
}
