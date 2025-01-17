package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GameUpdateDTO;
import com.example.BoardVerse.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/admin")
@Tag(name = "Admin", description = "Admin operations")
public class AdminController {

    private final GameService gameService;

    public AdminController(GameService gameService) {
        this.gameService = gameService;
    }

    // Endpoint per aggiungere un nuovo gioco
    @PostMapping("/games")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addNewGame(@RequestBody @Valid GameCreationDTO gameCreationDTO) {

        // Restituisce una risposta HTTP 201 Created passando il DTO al servizio
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.addNewGame(gameCreationDTO));
    }

    @PatchMapping("/games/{id}")
    public ResponseEntity<String> updateGame(@PathVariable String id, @RequestBody @Valid GameUpdateDTO gameUpdateDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.updateGame(id, gameUpdateDTO));
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.deleteGame(id));
    }

}
