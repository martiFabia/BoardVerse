package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Tournament.TournamentSuggestionDTO;
import com.example.BoardVerse.DTO.User.UserSimilarityDTO;
import com.example.BoardVerse.DTO.User.UserSuggestionDTO;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
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

    public List<UserSuggestionDTO> getSuggestedUsers(String username) {
       userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.suggestUsers(username);
    }

    public List<UserSimilarityDTO>  getSimilarUsers(String username) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.findSimilarUsersByGameTaste(username);
    }

    public List<TournamentSuggestionDTO> getSuggestedTournaments(String username) {
        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.suggestTournaments(username);
    }
}
