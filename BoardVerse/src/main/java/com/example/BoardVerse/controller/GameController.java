package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Game", description = "Operations related to games")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


@GetMapping("/searchByName/{name}")
public ResponseEntity<List<GameInfoDTO>> searchGamesByName(@PathVariable String name) {
    List<GameInfoDTO> games = gameService.findByName(name); // Trova i giochi per nome
    if (games.isEmpty()) {
        return ResponseEntity.notFound().build();  // Nessun gioco trovato
    }
    return ResponseEntity.ok(games);  // Restituisci i giochi trovati
}


@GetMapping("/searchByCategory/{category}")
public ResponseEntity<List<GameInfoDTO>> searchGamesByCategory(@PathVariable String category) {
    List<GameInfoDTO> games = gameService.findByCategory(category); // Trova i giochi per nome
    if (games.isEmpty()) {
        return ResponseEntity.notFound().build();  // Nessun gioco trovato
    }
    return ResponseEntity.ok(games);  // Restituisci i giochi trovati
}
}