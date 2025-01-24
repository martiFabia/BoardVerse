package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Tournament.TournamentSuggestionDTO;
import com.example.BoardVerse.DTO.User.UserSimilarityDTO;
import com.example.BoardVerse.DTO.User.UserSuggestionDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.SuggestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@Tag(name = "Suggestion", description = "Operations related to suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping("/suggestedUsers")
    public ResponseEntity<List<UserSuggestionDTO>> getSuggestedUsers() {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserSuggestionDTO> suggestedUsers = suggestionService.getSuggestedUsers(user.getUsername());
        return ResponseEntity.ok(suggestedUsers);
    }

    @GetMapping("/similarUsers")
    public ResponseEntity<List<UserSimilarityDTO>> getSimilarUsers() {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserSimilarityDTO> similarUsers = suggestionService.getSimilarUsers(user.getUsername());
        return ResponseEntity.ok(similarUsers);
    }

    @GetMapping("/suggestedTournaments")
    public ResponseEntity<List<TournamentSuggestionDTO>> getSuggestedTournaments() {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TournamentSuggestionDTO> suggestedTournaments = suggestionService.getSuggestedTournaments(user.getUsername());
        return ResponseEntity.ok(suggestedTournaments);
    }
}
