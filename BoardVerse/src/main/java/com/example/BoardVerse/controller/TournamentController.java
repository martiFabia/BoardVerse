package com.example.BoardVerse.controller;


import com.example.BoardVerse.DTO.Tournament.AddTournDTO;
import com.example.BoardVerse.DTO.Tournament.UpdateTournDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.TournamentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game/")
@Tag(name = "Tournament", description = "Operations related to tournaments")
public class TournamentController {


    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    /* ================================ TOURNAMENT CRUD ================================ */

    //crea torneo
    @PostMapping("/{gameId}/tournament")
    public ResponseEntity<String> createTournament(@PathVariable String gameId, @RequestBody @Valid AddTournDTO addTournDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            tournamentService.addTournament(gameId, user.getId(), addTournDTO);
            return ResponseEntity.ok("Tournament successfully added!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //elimina torneo
    @DeleteMapping("/{gameId}/tournament/{tournamentId}")
    public ResponseEntity<String> deleteTournament(@PathVariable String gameId, @PathVariable String tournamentId) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            tournamentService.deleteTournament(gameId, tournamentId, user.getUsername(), user.getUser().getTournaments());
            return ResponseEntity.ok("Tournament successfully deleted!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //aggiorna torneo
    @PatchMapping("/{gameId}/tournament/{tournamentId}")
    public ResponseEntity<String> updateTournament(@PathVariable String gameId, @PathVariable String tournamentId, @RequestBody @Valid UpdateTournDTO updateTournDTO) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            tournamentService.updateTournament(gameId, tournamentId, user.getUsername(), updateTournDTO);
            return ResponseEntity.ok("Tournament successfully updated!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    @GetMapping("/{gameId}/tournaments")
    public ResponseEntity<?> getTournaments(@PathVariable String gameId, @RequestParam(defaultValue = "0") int page) {
        try {
            return ResponseEntity.ok(tournamentService.getTournaments(gameId, page));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }
}