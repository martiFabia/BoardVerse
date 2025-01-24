package com.example.BoardVerse.service;


import com.example.BoardVerse.DTO.Tournament.AddTournDTO;
import com.example.BoardVerse.DTO.Tournament.TournPreview;
import com.example.BoardVerse.DTO.Tournament.TournamentDTO;
import com.example.BoardVerse.DTO.Tournament.UpdateTournDTO;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.Tournament;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.model.MongoDB.subentities.GameThread;
import com.example.BoardVerse.model.MongoDB.subentities.Role;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public void addTournament (String gameId, String userIdMongo, AddTournDTO addTournDTO) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        User userMongo = userMongoRepository.findById(userIdMongo)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userIdMongo));

        Tournament tournament = new Tournament();
        tournament.setId(UUID.randomUUID().toString());
        tournament.setGame(new GameThread(game.getId(), game.getName(), game.getYearReleased()));
        tournament.setAdministrator(userMongo.getUsername());
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
            List<String> allowed = addTournDTO.getAllowed(); // L'utente inserisce direttamente gli ID degli utenti

            // Recupera dalla collection `user` solo gli ID che corrispondono agli utenti esistenti
            List<String> existingIds = userMongoRepository.findUsersById(allowed);

            // Trova gli ID che non esistono confrontandoli con la lista degli ID forniti
            List<String> nonExistingIds = allowed.stream()
                    .filter(userId -> existingIds.stream().noneMatch(existingId -> existingId.equals(userId)))
                    .collect(Collectors.toList());

            // Se ci sono ID che non esistono, lancia un'eccezione
            if (!nonExistingIds.isEmpty()) {
                throw new NotFoundException("The following user IDs do not exist: " + nonExistingIds);
            }

            tournament.setAllowed(existingIds);
        }

        //Aumentare tournaments.created dell'user di uno
        userMongo.getTournaments().setCreated(userMongo.getTournaments().getCreated() + 1);

        userMongoRepository.save(userMongo);
        tournamentMongoRepository.save(tournament);

        // TODO AGGIUNGERE ANCHE SU GRAPH

    }


    public void deleteTournament(String gameId, String tournamentId, String userId, TournamentsUser tournamentsUser) {
        Tournament tournament = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found with ID: " + tournamentId));
        User user = userMongoRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        //Verificare che l'utente sia l'amministratore del torneo o sia un admin
        if (!tournament.getAdministrator().equals(user.getUsername()) || !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("You are not the administrator of this tournament and you are not an admin");
        }
        tournamentMongoRepository.deleteById(tournamentId);

        //Diminuire tournaments.created dell'user di uno
        user.getTournaments().setCreated(user.getTournaments().getCreated() - 1);

        userMongoRepository.save(user);

        //TODO RIMUOVERE ANCHE SU GRAPH
    }

    public void updateTournament(String gameId, String tournamentId, String username, UpdateTournDTO updateTournDTO){
        Tournament tournament = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found with ID: " + tournamentId));

        //Verificare che l'utente sia l'amministratore del torneo o sia un admin
        if (!tournament.getAdministrator().equals(username) || !userMongoRepository.findByUsername(username).get().getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("You are not the administrator of this tournament and you are not an admin");
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

        //TODO MODIFICARE IL GRAFO
    }

    public Slice<TournPreview> getTournaments(String gameId, String userId, int page) {
        return tournamentMongoRepository.findByGameOrderByStartingTimeDesc(gameId, userId, PageRequest.of(page, Constants.PAGE_SIZE));
    }

    public TournamentDTO getTournament(String tournamentId) {

        return tournamentMongoRepository.findById(tournamentId)
                .map(elem -> new TournamentDTO(elem.getName(), elem.getGame(),elem.getType(), elem.getTypeDescription(),
                        elem.getStartingTime(), elem.getLocation(), elem.getNumParticipants(), elem.getMinParticipants(),
                        elem.getMaxParticipants(), elem.getAdministrator(), elem.getWinner(), elem.getVisibility(),  elem.getOptions()))
                .orElseThrow(() -> new NotFoundException("Tournament not found with ID: " + tournamentId));
    }



}
