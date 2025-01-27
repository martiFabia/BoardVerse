package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.Tournament.AddTournamentDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentDTO;
import com.example.BoardVerse.model.MongoDB.TournamentMongo;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.model.MongoDB.subentities.GamePreviewEssential;
import com.example.BoardVerse.model.MongoDB.subentities.OptionsTournament;

import java.util.ArrayList;
import java.util.List;

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
        if (addTournamentDTO.getOptions() != null) {
            tournamentMongo.setOptions(addTournamentDTO.getOptions());
        }else
            tournamentMongo.setOptions(new ArrayList<OptionsTournament>());

        tournamentMongo.setStartingTime(addTournamentDTO.getStartingTime());
        tournamentMongo.setAllowed(null);

        return tournamentMongo;
    }

    public static TournamentDTO toTournamentDTO(TournamentMongo tournamentMongo) {

        return new TournamentDTO(
            tournamentMongo.getName(),
            tournamentMongo.getGame(),
            tournamentMongo.getType(),
            tournamentMongo.getTypeDescription(),
            tournamentMongo.getStartingTime(),
            tournamentMongo.getLocation(),
            tournamentMongo.getNumParticipants(),
            tournamentMongo.getMinParticipants(),
            tournamentMongo.getMaxParticipants(),
            tournamentMongo.getAdministrator(),
            tournamentMongo.getWinner(),
            tournamentMongo.getVisibility(),
            tournamentMongo.getOptions()
        );

    }

}
