package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Tournament.AddTournamentDTO;
import com.example.BoardVerse.model.MongoDB.TournamentMongo;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.model.MongoDB.subentities.GamePreviewEssential;

public class TournamentMapper {

    public static TournamentMongo toTournamentMongo(AddTournamentDTO addTournamentDTO, String id, GamePreviewEssential game, UserMongo userMongo) {

        TournamentMongo tournamentMongo = new TournamentMongo();

        tournamentMongo.setId(id);
        tournamentMongo.setGame(new GamePreviewEssential(game.getId(), game.getName(), game.getYearReleased()));
        tournamentMongo.setAdministrator(userMongo.getUsername());
        tournamentMongo.setName(addTournamentDTO.getName());
        tournamentMongo.setType(addTournamentDTO.getType());
        tournamentMongo.setTypeDescription(addTournamentDTO.getTypeDescription());
        tournamentMongo.setLocation(addTournamentDTO.getLocation());
        tournamentMongo.setNumParticipants(0);
        tournamentMongo.setMinParticipants(addTournamentDTO.getMinParticipants());
        tournamentMongo.setMaxParticipants(addTournamentDTO.getMaxParticipants());
        tournamentMongo.setVisibility(addTournamentDTO.getVisibility());
        tournamentMongo.setWinner(null);
        tournamentMongo.setOptions(addTournamentDTO.getOptions());
        tournamentMongo.setStartingTime(addTournamentDTO.getStartingTime());
        tournamentMongo.setAllowed(null);

        return tournamentMongo;
    }

}
