package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import lombok.AllArgsConstructor;

import java.util.UUID;

public class MongoGameMapper {

    // Converte un DTO in un'entità
    public static GameMongo mapDTOToGameMongo(GameCreationDTO newGameDTO) {

        GameMongo newGameMongo = new GameMongo();

        String gameId = UUID.randomUUID().toString();
        newGameMongo.setId(gameId);

        newGameMongo.setName(newGameDTO.getName());
        newGameMongo.setDescription(newGameDTO.getDescription());
        if( newGameDTO.getDescription() != null && newGameDTO.getDescription().length() > 20)
            newGameMongo.setShortDescription(newGameDTO.getDescription().substring(0, 20) + "...");
        else
            newGameMongo.setShortDescription(newGameDTO.getDescription());
        newGameMongo.setImgURL(newGameDTO.getImgURL());
        newGameMongo.setAverageRating(0);
        newGameMongo.setNumberReviews(0);
        newGameMongo.setYearReleased(newGameDTO.getYearReleased());
        newGameMongo.setMinPlayers(newGameDTO.getMinPlayers());
        newGameMongo.setMaxPlayers(newGameDTO.getMaxPlayers());
        newGameMongo.setMinSuggAge(newGameDTO.getMinSuggAge());
        newGameMongo.setMinPlayTime(newGameDTO.getMinPlayTime());
        newGameMongo.setMaxPlayTime(newGameDTO.getMaxPlayTime());
        newGameMongo.setDesigners(newGameDTO.getDesigners());
        newGameMongo.setArtists(newGameDTO.getArtists());
        newGameMongo.setPublisher(newGameDTO.getPublisher());
        newGameMongo.setCategories(newGameDTO.getCategories());
        newGameMongo.setMechanics(newGameDTO.getMechanics());

        return newGameMongo;
    }

    // Converte un'entità in un DTO

    public static GameInfoDTO toDTO(GameMongo gameMongo) {
        return new GameInfoDTO(
            gameMongo.getName(),
            gameMongo.getDescription(),
            gameMongo.getShortDescription(),
            gameMongo.getImgURL(),
            gameMongo.getAverageRating(),
            gameMongo.getNumberReviews(),
            gameMongo.getYearReleased(),
            gameMongo.getMinPlayers(),
            gameMongo.getMaxPlayers(),
            gameMongo.getMinSuggAge(),
            gameMongo.getMinPlayTime(),
            gameMongo.getMaxPlayTime(),
            gameMongo.getDesigners(),
            gameMongo.getArtists(),
            gameMongo.getPublisher(),
            gameMongo.getCategories(),
            gameMongo.getMechanics()
        );
    }
}

