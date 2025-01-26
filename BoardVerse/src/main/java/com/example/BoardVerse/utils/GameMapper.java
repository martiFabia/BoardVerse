package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Game.GameCreationDTO;
import com.example.BoardVerse.DTO.Game.GameInfoDTO;
import com.example.BoardVerse.DTO.Game.GameRankPreviewDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.subentities.GamePreviewEssential;
import com.example.BoardVerse.model.Neo4j.GameNeo4j;

import java.util.Date;

public class GameMapper {

    // Converte un DTO in un'entità
    public static GameMongo toGameMongo(GameCreationDTO newGameDTO, String gameId) {

        GameMongo newGameMongo = new GameMongo();

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

    public static GameNeo4j toGameNeo4j(GameCreationDTO gameMongo, String gameId) {
        GameNeo4j newGameNeo4j = new GameNeo4j();

        newGameNeo4j.setId(gameId);
        newGameNeo4j.setName(gameMongo.getName());
        newGameNeo4j.setYearReleased(gameMongo.getYearReleased());
        newGameNeo4j.setShortDescription(gameMongo.getShortDescription());
        newGameNeo4j.setCategories(gameMongo.getCategories());

        return newGameNeo4j;
    }

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

    public static GameRankPreviewDTO toGameRankPreviewDTO(GameMongo gameMongo) {
        return new GameRankPreviewDTO(
            gameMongo.getId(),
            gameMongo.getName(), gameMongo.getYearReleased(),
            gameMongo.getShortDescription(),
            gameMongo.getAverageRating()
        );
    }

    public static GamePreviewEssential toPreviewEssential(GameMongo gameMongo) {
        return new GamePreviewEssential(
            gameMongo.getId(),
            gameMongo.getName(),
            gameMongo.getYearReleased()
        );
    }

}

