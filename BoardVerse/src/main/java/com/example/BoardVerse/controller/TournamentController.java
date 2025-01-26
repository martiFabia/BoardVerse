package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Tournament.AddTournDTO;
import com.example.BoardVerse.DTO.Tournament.UpdateTournDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> createTournament(@PathVariable String gameId, @RequestBody @Valid AddTournDTO addTournDTO) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(tournamentService.addTournament(gameId, user.getId(), addTournDTO));
    }

    @Operation(summary = "Delete a tournament")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{gameId}/tournament/{tournamentId}")
    public ResponseEntity<String> deleteTournament(@PathVariable String gameId, @PathVariable String tournamentId) {

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.deleteTournament(gameId, tournamentId, user.getUsername(), user.getUser().getTournaments()));

    }

    @Operation(summary = "Update a tournament")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{gameId}/tournament/{tournamentId}")
    public ResponseEntity<String> updateTournament(@PathVariable String gameId, @PathVariable String tournamentId, @RequestBody @Valid UpdateTournDTO updateTournDTO) {

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(tournamentService.updateTournament(gameId, tournamentId, user.getUsername(), updateTournDTO));

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
}