package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.DTO.Game.GameRankPreviewDTO;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.GameService;
import com.example.BoardVerse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Game", description = "Operations related to games")
public class GameController {
    private final GameService gameService;
    private final UserService userService;

    @Autowired
    public GameController(GameService gameService, UserService userService) {

        this.gameService = gameService;
        this.userService = userService;
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

    @Operation(summary = "Get games statistics about likes and tournaments")
    @GetMapping("{gameId}/statistics")
    public ResponseEntity<?> getStatistics(@PathVariable String gameId){
        return ResponseEntity.ok(gameService.getStatistics(gameId));
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

    @Operation(summary = "Hottest games based on threads activity")
    @GetMapping("/hottest")
    public ResponseEntity<?> getHottestGames( @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
                                              @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(gameService.getBestGamesByThread(startDate, endDate, page));
    }


    /*================================ LIKES =================================*/

    @Operation(summary = "Like a game")
    @PostMapping("/{gameId}/like")
    public ResponseEntity<String> likeGame(@PathVariable String gameId){
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.likeGame(gameId, user.getId()));
    }

    @Operation(summary = "Unlike a game")
    @DeleteMapping("/{gameId}/like")
    public ResponseEntity<String> unlikeGame(@PathVariable String gameId){
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.unlikeGame(gameId, user.getId()));
    }

    @Operation(summary = "Get the list of user who liked a game")
    @GetMapping("/{gameId}/likes")
    public ResponseEntity<?> getLikes(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "alphabetical") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(gameService.getLikes(gameId, sortBy, page, size));
    }

}