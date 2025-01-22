package com.example.BoardVerse.service;


import com.example.BoardVerse.DTO.Tournament.AddTournDTO;
import com.example.BoardVerse.DTO.Tournament.TournPreview;
import com.example.BoardVerse.DTO.Tournament.UpdateTournDTO;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.Tournament;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.model.MongoDB.subentities.GameThread;
import com.example.BoardVerse.model.MongoDB.subentities.TournamentsUser;
import com.example.BoardVerse.model.MongoDB.subentities.VisibilityTournament;
import com.example.BoardVerse.repository.GameMongoRepository;
import com.example.BoardVerse.repository.TournamentMongoRepository;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.utils.Constants;
import org.springframework.aot.generate.AccessControl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TournamentService {

    private TournamentMongoRepository tournamentMongoRepository;;
    private final GameMongoRepository gameMongoRepository;
    private UserMongoRepository userMongoRepository;

    public TournamentService(TournamentMongoRepository tournamentMongoRepository, GameMongoRepository gameRepository, UserMongoRepository userMongoRepository) {
        this.tournamentMongoRepository = tournamentMongoRepository;
        this.gameMongoRepository = gameRepository;
        this.userMongoRepository = userMongoRepository;

    }

    public void addTournament (String gameId, String userId, AddTournDTO addTournDTO) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        User user = userMongoRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Tournament tournament = new Tournament();
        tournament.setId(UUID.randomUUID().toString());
        tournament.setGame(new GameThread(game.getId(), game.getName(), game.getYearReleased()));
        tournament.setAdministrator(user.getUsername());
        tournament.setName(addTournDTO.getName());
        tournament.setType(addTournDTO.getType());
        tournament.setTypeDescription(addTournDTO.getTypeDescription());
        tournament.setLocation(addTournDTO.getLocation());
        tournament.setNumParticipants(0);
        tournament.setMinParticipants(addTournDTO.getMinParticipants());
        tournament.setMaxParticipants(addTournDTO.getMaxParticipants());
        tournament.setVisibility(addTournDTO.getVisibility());
        tournament.setWinner(null);
        tournament.setOptions(addTournDTO.getOptions());
        tournament.setStartingTime(addTournDTO.getStartingTime());
        if (addTournDTO.getVisibility() == VisibilityTournament.PUBLIC || addTournDTO.getVisibility() == VisibilityTournament.INVITE) {
            tournament.setAllowed(null);
        } else {
            tournament.setAllowed(addTournDTO.getAllowed());
        }
        //Aumentare tournaments.created dell'user di uno
        user.getTournaments().setCreated(user.getTournaments().getCreated() + 1);

        userMongoRepository.save(user);
        tournamentMongoRepository.save(tournament);

        //AGGIUNGERE ANCHE SU GRAPH

    }


    public void deleteTournament(String gameId, String tournamentId, String userId, TournamentsUser tournamentsUser) {
        Tournament tournament = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found with ID: " + tournamentId));
        User user = userMongoRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (!tournament.getAdministrator().equals(user.getUsername())) {
            throw new IllegalArgumentException("You are not the administrator of this tournament");
        }
        tournamentMongoRepository.deleteById(tournamentId);

        //Diminuire tournaments.created dell'user di uno
        user.getTournaments().setCreated(user.getTournaments().getCreated() - 1);

        userMongoRepository.save(user);

        //RIMUOVERE ANCHE SU GRAPH
    }

    public void updateTournament(String gameId, String tournamentId, String username, UpdateTournDTO updateTournDTO){
        Tournament tournament = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found with ID: " + tournamentId));

        if (!tournament.getAdministrator().equals(username)) {
            throw new IllegalArgumentException("You are not the administrator of this tournament");
        }

        if(updateTournDTO.getName() != null){
            tournament.setName(updateTournDTO.getName());
        }
        if(updateTournDTO.getType() != null){
            tournament.setType(updateTournDTO.getType());
        }
        if(updateTournDTO.getTypeDescription() != null){
            tournament.setTypeDescription(updateTournDTO.getTypeDescription());
        }
        if(updateTournDTO.getStartingTime() != null){
            tournament.setStartingTime(updateTournDTO.getStartingTime());
        }
        if(updateTournDTO.getLocation() != null){
            tournament.setLocation(updateTournDTO.getLocation());
        }
        if(updateTournDTO.getMinParticipants() != null){
            tournament.setMinParticipants(updateTournDTO.getMinParticipants());
        }
        if(updateTournDTO.getMaxParticipants() != null){
            tournament.setMaxParticipants(updateTournDTO.getMaxParticipants());
        }
        if(updateTournDTO.getVisibility() != null){
            tournament.setVisibility(updateTournDTO.getVisibility());
        }
        if(updateTournDTO.getWinner() != null){
            String usernameWinner = updateTournDTO.getWinner();
            tournament.setWinner(usernameWinner);

            //aumentare di uno tournaments.won dell'user indicato
            User user = userMongoRepository.findByUsername(usernameWinner)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + usernameWinner));
            user.getTournaments().setWon(user.getTournaments().getWon() + 1);
            userMongoRepository.save(user);
        }
        if(updateTournDTO.getOptions() != null){
            tournament.setOptions(updateTournDTO.getOptions());
        }

        tournamentMongoRepository.save(tournament);

        //MODIFICARE IL GRAFO
    }


    public Slice<TournPreview> getTournaments(String gameId, int page) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        return tournamentMongoRepository.findByGameOrderByStartingTimeDesc(gameId, PageRequest.of(page, Constants.PAGE_SIZE));
    }



}
