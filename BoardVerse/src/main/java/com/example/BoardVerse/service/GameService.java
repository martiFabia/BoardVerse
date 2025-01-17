package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.DTO.Game.GameUpdateDTO;
import com.example.BoardVerse.exception.GameNotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.repository.GameMongoRepository;
import com.example.BoardVerse.utils.MongoGameMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.BoardVerse.utils.MongoGameMapper.mapDTOToGameMongo;

@Service
public class GameService {

    private final GameMongoRepository gameMongoRepository;

    public GameService(GameMongoRepository gameMongoRepository) {
        this.gameMongoRepository = gameMongoRepository;
    }

    // Operazione di creazione
    public String addNewGame(GameCreationDTO newGameDTO) {
        if (gameMongoRepository.findByNameAndYearReleased(newGameDTO.getName(), newGameDTO.getYearReleased()).isPresent()) {
            throw new GameNotFoundException("Game " + newGameDTO.getName() + " released in " + newGameDTO.getYearReleased() + " already exists");
        }
        GameMongo newGameMongo = mapDTOToGameMongo(newGameDTO);
        gameMongoRepository.save(newGameMongo);
        return "Game " + newGameMongo.getName() + " released in " + newGameDTO.getYearReleased() + " added successfully";

    }


    public String updateGame(String gameId, GameUpdateDTO updateGameDTO) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));


        //Se è presente un gioco con lo stesso nome e anno di rilascio,
        // ma questo gioco non è quello che si sta cercando di aggiornare
        // allora lancia un'eccezione
        Optional<GameMongo> existingGame = gameMongoRepository.findByNameAndYearReleased(updateGameDTO.getName(), updateGameDTO.getYearReleased());
        if (existingGame.isPresent() && !existingGame.get().getId().equals(gameId)) {
            throw new GameNotFoundException("Game " + updateGameDTO.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists.");
        }

        game.setName(updateGameDTO.getName());

        if(updateGameDTO.getDescription() != null){
        game.setDescription(updateGameDTO.getDescription());
        if (updateGameDTO.getDescription().length() > 20)
            game.setShortDescription(updateGameDTO.getDescription().substring(0, 20) + "...");
        else
            game.setShortDescription(updateGameDTO.getDescription());
        }
        
        game.setImgURL(updateGameDTO.getImgURL());

        game.setYearReleased(updateGameDTO.getYearReleased());

        if(updateGameDTO.getMinPlayers()!=null) {
            game.setMinPlayers(updateGameDTO.getMinPlayers());
        }

        if(updateGameDTO.getMaxPlayers()!=null) {
            game.setMaxPlayers(updateGameDTO.getMaxPlayers());
        }

        if(updateGameDTO.getMinSuggAge()!=null) {
            game.setMinSuggAge(updateGameDTO.getMinSuggAge());
        }

        if (updateGameDTO.getMinPlayTime()!=null) {
            game.setMinPlayTime(updateGameDTO.getMinPlayTime());
        }

        if (updateGameDTO.getMaxPlayTime()!=null) {
            game.setMaxPlayTime(updateGameDTO.getMaxPlayTime());
        }

        game.setDesigners(updateGameDTO.getDesigners());
        game.setArtists(updateGameDTO.getArtists());
        game.setPublisher(updateGameDTO.getPublisher());

        if(updateGameDTO.getCategories()!=null && !updateGameDTO.getCategories().isEmpty()) {
            game.setCategories(updateGameDTO.getCategories());
        }
        game.setMechanics(updateGameDTO.getMechanics());

        gameMongoRepository.save(game);
        return "Game " + game.getId() + " updated successfully";
    }


    // Operazione di eliminazione
    public String deleteGame(String gameId) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));
        gameMongoRepository.delete(game);
        return "Game with id " + gameId + " deleted successfully";

    }

    // Operazione di ricerca per nome
    public List<GameInfoDTO> findByName(String name) {
        List<GameMongo> games = gameMongoRepository.findByName(name);

        return games.stream().map(MongoGameMapper::toDTO).collect(Collectors.toList());
    }

    // Operazione di ricerca per categoria
    public List<GameInfoDTO> findByCategory(String category) {
        List<GameMongo> games = gameMongoRepository.findByCategoriesContaining(category);

        return games.stream().map(MongoGameMapper::toDTO).collect(Collectors.toList());
    }
}