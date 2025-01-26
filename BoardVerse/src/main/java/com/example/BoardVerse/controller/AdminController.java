package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GameUpdateDTO;
import com.example.BoardVerse.service.AnalyticsService;
import com.example.BoardVerse.service.GameService;
import com.example.BoardVerse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Add a new game")
    @PostMapping("/games")
    public ResponseEntity<String> addNewGame(@RequestBody @Valid GameCreationDTO gameCreationDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.addNewGame(gameCreationDTO));
    }


    @Operation(summary = "Update a game")
    @PatchMapping("/games/{id}")
    public ResponseEntity<String> updateGame(@PathVariable String id, @RequestBody @Valid GameUpdateDTO gameUpdateDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.updateGame(id, gameUpdateDTO));
    }


    @Operation(summary = "Delete a game")
    @DeleteMapping("/games/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.deleteGame(id));
    }

    /*--------------------------------USER ---------------------------------*/

    @Operation(summary = "Delete a user")
    @DeleteMapping("/users/{username}")
    public ResponseEntity<String> deleteUserByAdmin(@PathVariable String username) {

        return ResponseEntity.ok(userService.deleteUser(username));
    }


    /*--------------------------------ADMIN ANALYTICS ---------------------------------*/

    @Operation(summary = "Get best games by age")
    @GetMapping("/analytics/bestGamesByAge")
    public ResponseEntity<?> bestGamesByAge() {
        return ResponseEntity.ok(gameService.bestGamesByAge());
    }

    @Operation(summary = "Get number of users by country")
    @GetMapping("/analytics/usersByLocation")
    public ResponseEntity<?> usersByLocation(@RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(analyticsService.usersByLocation(page));

    }

    @Operation(summary = "Get monthly registrations by year")
    @GetMapping("/analytics/monthlyRegistrations")
    public ResponseEntity<?> monthlyRegistrations(@RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(analyticsService.monthlyRegistrations(year));
    }


}
