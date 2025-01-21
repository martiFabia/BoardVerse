package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.DTO.Game.GamePreviewDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.Review;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.UUID;

public class MongoGameMapper {

    // Converte un DTO in un'entità
    public static GameMongo mapDTOToGameMongo(GameCreationDTO newGameDTO) {

        GameMongo newGameMongo = new GameMongo();

        String gameId = UUID.randomUUID().toString();

        //AGGIUNGERE GAME AL GRAPH

        newGameMongo.setId(gameId);
        newGameMongo.setName(newGameDTO.getName());
        newGameMongo.setDescription(newGameDTO.getDescription());
        newGameMongo.setShortDescription(newGameDTO.getShortDescription());
        newGameMongo.setAverageRating(0.0);
        newGameMongo.setRatingVoters(0);
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
        newGameMongo.setFamily(newGameDTO.getFamily());
        newGameMongo.setUploadTime(new Date());

        return newGameMongo;
    }

    // Converte un'entità in un DTO

    public static GameInfoDTO toDTO(GameMongo gameMongo) {
        return new GameInfoDTO(
            gameMongo.getName(),
            gameMongo.getDescription(),
            gameMongo.getShortDescription(),
            gameMongo.getAverageRating(),
            gameMongo.getRatingVoters(),
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
            gameMongo.getMechanics(),
            gameMongo.getFamily()
        );
    }

    public static GamePreviewDTO toPreviewDTO(GameMongo gameMongo) {
        return new GamePreviewDTO(
            gameMongo.getId(),
            gameMongo.getName(), gameMongo.getYearReleased(),
            gameMongo.getShortDescription(),
            gameMongo.getAverageRating()
        );
    }




}

