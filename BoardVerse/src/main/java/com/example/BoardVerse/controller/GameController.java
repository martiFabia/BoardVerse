package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.DTO.Game.GamePreviewDTO;
import com.example.BoardVerse.DTO.User.UserInfoDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/search/{gameName}")
    public ResponseEntity<List<GamePreviewDTO>> searchGamesByName(@PathVariable String gameName) {
        List<GamePreviewDTO> games = gameService.findByName(gameName); // Trova i giochi per nome
        if (games.isEmpty()) {
            return ResponseEntity.notFound().build();  // Nessun gioco trovato
        }
        return ResponseEntity.ok(games);  // Restituisci i giochi trovati
    }

    //restituisce tutti i dati dell'utente tranne la password
    @GetMapping("/{gameId}/getInfo")
    public ResponseEntity<GameInfoDTO> getUserInfoByUsername(@PathVariable String gameId) {
        GameInfoDTO gameInfo= gameService.getInfo(gameId);
        return ResponseEntity.ok(gameInfo);
    }


    /*
    @GetMapping("/searchByCategory/{category}")
    public ResponseEntity<List<GameInfoDTO>> searchGamesByCategory(@PathVariable String category) {
        List<GameInfoDTO> games = gameService.findByCategory(category); // Trova i giochi per nome
        if (games.isEmpty()) {
            return ResponseEntity.notFound().build();  // Nessun gioco trovato
        }
        return ResponseEntity.ok(games);  // Restituisci i giochi trovati
    }
     */
}