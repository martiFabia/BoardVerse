package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.GameCreationDTO;
import com.example.BoardVerse.model.MongoDB.Game;

public class GameMapper {

    // Converte un DTO in un'entità
    public static Game toEntity(GameCreationDTO dto) {
        Game game = new Game();
        game.setName(dto.getName());
        game.setDescription(dto.getDescription());
        game.setShortDescription(dto.getShortDescription());
        game.setImgURL(dto.getImgURL());
        game.setAverageRating(dto.getAverageRating());
        game.setNumberReviews(dto.getNumberReviews());
        game.setYearReleased(dto.getYearReleased());
        game.setMinPlayers(dto.getMinPlayers());
        game.setMaxPlayers(dto.getMaxPlayers());
        game.setMinSuggAge(dto.getMinSuggAge());
        game.setMinPlayTime(dto.getMinPlayTime());
        game.setMaxPlayTime(dto.getMaxPlayTime());
        game.setDesigners(dto.getDesigners());
        game.setArtists(dto.getArtists());
        game.setPublisher(dto.getPublisher());
        game.setCategories(dto.getCategories());
        game.setMechanics(dto.getMechanics());
        return game;
    }
}

