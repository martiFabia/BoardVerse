package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.GameCreationDTO;
import com.example.BoardVerse.model.MongoDB.Game;
import com.example.BoardVerse.service.GameService;
import com.example.BoardVerse.utils.GameMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/admin")
@Tag(name = "Admin", description = "Admin operations")
public class AdminController {

    private final GameService gameService;

    public AdminController(GameService gameService) {
        this.gameService = gameService;
    }

    // Endpoint per aggiungere un nuovo gioco
    @PostMapping("/game")
    public ResponseEntity<Game> addNewGame(@RequestBody @Valid GameCreationDTO gameCreationDTO) {
        // Converte il DTO in un'entità Game
        Game game = GameMapper.toEntity(gameCreationDTO);
        // Salva il gioco utilizzando il servizio
        Game savedGame = gameService.addNewGame(game);
        // Restituisce una risposta HTTP 201 Created con il gioco salvato
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
    }
}
