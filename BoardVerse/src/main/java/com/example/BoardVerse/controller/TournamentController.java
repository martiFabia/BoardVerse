package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Tournament.AddTournamentDTO;
import com.example.BoardVerse.DTO.Tournament.UpdateTournamentDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/")
@Tag(name = "Tournament", description = "Operations related to tournaments")
public class TournamentController {


    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    /* ================================ TOURNAMENT CRUD ================================ */

    @Operation(summary = "Add a new tournament")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{gameId}/tournament")
    public ResponseEntity<String> createTournament(@PathVariable String gameId, @RequestBody @Valid AddTournamentDTO addTournamentDTO) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(tournamentService.addTournament(gameId, user.getId(), addTournamentDTO));
    }

    @Operation(summary = "Delete a tournament")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{gameId}/tournament/{tournamentId}")
    public ResponseEntity<String> deleteTournament(@PathVariable String gameId, @PathVariable String tournamentId) {

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.deleteTournament(tournamentId, user.getUsername()));

    }

    @Operation(summary = "Update a tournament")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{gameId}/tournament/{tournamentId}")
    public ResponseEntity<String> updateTournament(@PathVariable String gameId, @PathVariable String tournamentId, @RequestBody @Valid UpdateTournamentDTO updateTournamentDTO) {

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.updateTournament(gameId, tournamentId, user.getUsername(), updateTournamentDTO));

    }

    @Operation(summary = "Get game tournaments")
    @GetMapping("/{gameId}/tournaments")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTournaments(@PathVariable String gameId, @RequestParam(defaultValue = "0") int page) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.getTournaments(gameId, user.getId(), page));
    }

    @Operation(summary = "Get tournament detail")
    @GetMapping("/tournaments/{tournamentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTournament(@PathVariable String tournamentId) {
        return ResponseEntity.ok(tournamentService.getTournament(tournamentId));
    }

    @Operation(summary = "Get tournament participants")
    @GetMapping("/tournaments/{tournamentId}/participants")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentParticipants(
            @PathVariable String tournamentId,
            @RequestParam(defaultValue = "alphabetical") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        return ResponseEntity.ok(tournamentService.getTournamentParticipants(tournamentId, sortBy, page, size));
    }

    @Operation(summary = "Register to a tournament")
    @PostMapping("/tournaments/{tournamentId}/register")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> registerToTournament(@PathVariable String tournamentId) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.registerToTournament(tournamentId, user.getId()));
    }

    @Operation(summary = "Unregister from a tournament")
    @DeleteMapping("/tournaments/{tournamentId}/unregister")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> unregisterFromTournament(@PathVariable String tournamentId) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.unregisterFromTournament(tournamentId, user.getId()));
    }

    @Operation(summary = "Select a winner for a tournament")
    @PostMapping("/tournaments/{tournamentId}/selectWinner")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> selectWinner(
            @PathVariable String tournamentId,
            @RequestParam String winnerUsername) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.selectWinner(tournamentId, winnerUsername, user.getUsername()));
    }

    @Operation(summary = "Get tournament difficulty index")
    @GetMapping("/tournaments/{tournamentId}/difficulty")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentDifficultyIndex(@PathVariable String tournamentId) {
        return ResponseEntity.ok(tournamentService.getTournamentDifficultyIndex(tournamentId));
    }

    @Operation(summary = "Get tournament social density index")
    @GetMapping("/tournaments/{tournamentId}/socialDensity")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentSocialDensityIndex(@PathVariable String tournamentId) {
        return ResponseEntity.ok(tournamentService.getTournamentSocialDensityIndex(tournamentId));
    }

}