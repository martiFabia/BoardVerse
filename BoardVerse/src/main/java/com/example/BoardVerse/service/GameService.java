package com.example.BoardVerse.service;

import com.example.BoardVerse.exception.GameNotFoundException;
import com.example.BoardVerse.model.MongoDB.Game;
import com.example.BoardVerse.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    // Operazione di creazione
    public Game addNewGame(Game game) {
       // if (gameRepository.findById(game.getId()).isPresent()) {
            //throw new DuplicateKeyException("Game already exists with id: " + game.getId());
       // }
        return gameRepository.save(game);
    }

    // Operazione di ricerca per nome
    public List<Game> findByName(String name) {
        List<Game> gamesFound = gameRepository.findByName(name);
        if (gamesFound.isEmpty()) {
            throw new GameNotFoundException("No game found with name: " + name);
        }
        return gamesFound;
    }

    // Operazione di ricerca per categoria
    public List<Game> findByCategory(String category) {
        List<Game> gamesFound = gameRepository.findByCategoriesContaining(category);
        if (gamesFound.isEmpty()) {
            throw new GameNotFoundException("No game found with category: " + category);
        }
        return gamesFound;
    }

    // Operazione di eliminazione
    public void deleteGame(String id) {
        if (gameRepository.findById(id).isEmpty()) {
            throw new GameNotFoundException("Game not found with id: " + id);
        }
        gameRepository.deleteById(id);
    }
}
