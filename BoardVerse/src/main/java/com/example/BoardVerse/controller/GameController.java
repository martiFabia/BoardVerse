package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.DTO.Game.GameRankPreviewDTO;
import com.example.BoardVerse.DTO.Game.MostPlayedGameDTO;
import com.example.BoardVerse.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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

    @Operation(summary = "Browse games")
    @GetMapping("/browse")
    public ResponseEntity<?> getGamesByName(@RequestParam(defaultValue = "") String gameName,
                                                                  @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(gameService.findByName(gameName, page));
    }


    @Operation(summary = "Get game page")
    @GetMapping("/{gameId}")
    public ResponseEntity<GameInfoDTO> getUserInfoByUsername(@PathVariable String gameId) {
        GameInfoDTO gameInfo= gameService.getInfo(gameId);
        return ResponseEntity.ok(gameInfo);
    }

    @Operation(summary = "Get game ratings detail")
    @GetMapping("{gameId}/ratingsDetail")
    public ResponseEntity<?> getRatingsDetail(@PathVariable String gameId){
        return ResponseEntity.ok(gameService.getRatingsDetails(gameId));

    }


    @Operation(summary = "Find games by filter")
    @GetMapping("/filter")
    public ResponseEntity<Slice<GameRankPreviewDTO>> filterGames(
            @RequestParam(required = false) Integer yearReleased,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String mechanic,
            @RequestParam(defaultValue = "averageRating") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(gameService.getFilteredGames(
                yearReleased, category, mechanic,
                sortBy, order, page));
    }


    @Operation(summary = "Get games ranking")
    @GetMapping("/ranking")
    public ResponseEntity<Slice<GameRankPreviewDTO>> getRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page){

        return ResponseEntity.ok(gameService.getRanking(startDate, endDate, country, state, city, page));
    }


    @Operation(summary = "Get the best 10 most played games")
    @GetMapping("/mostPlayed")
    public ResponseEntity<?> getMostPlayedGames(){
            return ResponseEntity.ok(gameService.getMostPlayedGames());

    }

}