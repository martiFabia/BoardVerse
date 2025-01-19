package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.DTO.Game.GamePreviewDTO;
import com.example.BoardVerse.DTO.Game.GameUpdateDTO;
import com.example.BoardVerse.exception.GameNotFoundException;
import com.example.BoardVerse.exception.NotFoundException;
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
            throw new NotFoundException("Game " + newGameDTO.getName() + " released in " + newGameDTO.getYearReleased() + " already exists");
        }
        GameMongo newGameMongo = mapDTOToGameMongo(newGameDTO);
        gameMongoRepository.save(newGameMongo);
        return "Game " + newGameMongo.getName() + " released in " + newGameDTO.getYearReleased() + " added successfully";

    }


    public String updateGame(String gameId, GameUpdateDTO updateGameDTO) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));


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
        }

        if(updateGameDTO.getYearReleased() != null)
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

        if (updateGameDTO.getShortDescription()!=null) {
            game.setShortDescription(updateGameDTO.getShortDescription());
        }

        if (updateGameDTO.getDesigners()!=null) {
            game.setDesigners(updateGameDTO.getDesigners());
        }
        if (updateGameDTO.getArtists()!=null)
            game.setArtists(updateGameDTO.getArtists());

        if (updateGameDTO.getPublisher()!=null)
            game.setPublisher(updateGameDTO.getPublisher());

        if(updateGameDTO.getCategories()!=null && !updateGameDTO.getCategories().isEmpty()) {
            game.setCategories(updateGameDTO.getCategories());
        }

        if(updateGameDTO.getFamily()!=null)
            game.setFamily(updateGameDTO.getFamily());

        if(updateGameDTO.getMechanics()!=null)
            game.setMechanics(updateGameDTO.getMechanics());

        gameMongoRepository.save(game);
        return "Game " + game.getId() + " updated successfully";
    }


    // Operazione di eliminazione
    public String deleteGame(String gameId) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        gameMongoRepository.delete(game);
        return "Game with id " + gameId + " deleted successfully";

    }

    // Operazione di ricerca per nome
    public List<GamePreviewDTO> findByName(String name) {
        List<GameMongo> games = gameMongoRepository.findByName(name);

        return games.stream().map(MongoGameMapper::toPreviewDTO).collect(Collectors.toList());
    }

    public GameInfoDTO getInfo(String gameId) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        return MongoGameMapper.toDTO(game);
    }


    // Operazione di ricerca per categoria
    /*
    public List<GameInfoDTO> findByCategory(String category) {
        List<GameMongo> games = gameMongoRepository.findByCategoriesContaining(category);

        return games.stream().map(MongoGameMapper::toDTO).collect(Collectors.toList());
    }

     */
}