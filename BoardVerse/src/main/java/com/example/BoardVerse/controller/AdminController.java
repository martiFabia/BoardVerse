package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Game.BestGameAgeDTO;
import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GamePreviewDTO;
import com.example.BoardVerse.DTO.Game.GameUpdateDTO;
import com.example.BoardVerse.DTO.User.aggregation.CountryAggregation;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.AnalyticsService;
import com.example.BoardVerse.service.GameService;
import com.example.BoardVerse.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/admin")
@Tag(name = "Admin", description = "Admin operations")
public class AdminController {

    private final GameService gameService;
    private final AnalyticsService analyticsService;
    private final UserService userService;

    public AdminController(GameService gameService, AnalyticsService analyticsService, UserService userService) {
        this.gameService = gameService;
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    /*--------------------------------GAME CRUD ---------------------------------*/
    // Endpoint per aggiungere un nuovo gioco
    @PostMapping("/games")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addNewGame(@RequestBody @Valid GameCreationDTO gameCreationDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.addNewGame(gameCreationDTO));
    }


    //modifica gioco
    @PatchMapping("/games/{id}")
    public ResponseEntity<String> updateGame(@PathVariable String id, @RequestBody @Valid GameUpdateDTO gameUpdateDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.updateGame(id, gameUpdateDTO));
    }

    //elimina gioco
    @DeleteMapping("/games/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.deleteGame(id));
    }

    /*--------------------------------USER ---------------------------------*/

    @DeleteMapping("/users/{username}")
    public ResponseEntity<String> deleteUserByAdmin(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully by admin");
    }


    /*--------------------------------ADMIN ANALYTICS ---------------------------------*/


    // best giochi per fascia di età
    @GetMapping("/analytics/bestGamesByAge")
    public ResponseEntity<?> bestGamesByAge() {
        try {
            return ResponseEntity.ok(gameService.bestGamesByAge());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //utenti per location
    @GetMapping("/analytics/usersByLocation")
    public ResponseEntity<?> usersByLocation(@RequestParam(defaultValue = "0") int page) {
        try {
            return ResponseEntity.ok(analyticsService.usersByLocation(page));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

    //registrazioni mensili utenti
    @GetMapping("/analytics/monthlyRegistrations")
    public ResponseEntity<?> monthlyRegistrations(@RequestParam(required = false) Integer year) {
        try {
            return ResponseEntity.ok(analyticsService.monthlyRegistrations(year));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: An unexpected error occurred.");
        }
    }

}
