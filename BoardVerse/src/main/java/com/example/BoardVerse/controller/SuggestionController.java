package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Game.GameSuggestionDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentSuggestionDTO;
import com.example.BoardVerse.DTO.User.UserFollowRecommendationDTO;
import com.example.BoardVerse.DTO.User.UserTastesSuggestionDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.SuggestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<UserFollowRecommendationDTO>> getSuggestedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserFollowRecommendationDTO> suggestedUsers = suggestionService.getSuggestedUsers(user.getUsername(), page, size);
        return ResponseEntity.ok(suggestedUsers);
    }

    @GetMapping("/similarUsers")
    public ResponseEntity<List<UserTastesSuggestionDTO>> getSimilarUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserTastesSuggestionDTO> similarUsers = suggestionService.getSimilarUsers(user.getUsername(), page, size);
        return ResponseEntity.ok(similarUsers);
    }

    @GetMapping("/suggestedTournaments")
    public ResponseEntity<List<TournamentSuggestionDTO>> getSuggestedTournaments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TournamentSuggestionDTO> suggestedTournaments = suggestionService.getSuggestedTournaments(user.getUsername(), page, size);
        return ResponseEntity.ok(suggestedTournaments);
    }

    @GetMapping("/suggestedGames")
    public ResponseEntity<List<GameSuggestionDTO>> getSuggestedGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<GameSuggestionDTO> suggestedGames = suggestionService.getSuggestedGames(user.getUsername(), page, size);
        return ResponseEntity.ok(suggestedGames);
    }
}
