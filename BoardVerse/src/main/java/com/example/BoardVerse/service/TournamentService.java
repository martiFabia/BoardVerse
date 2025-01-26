package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Tournament.AddTournamentDTO;
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
import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import com.example.BoardVerse.repository.*;
import com.example.BoardVerse.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing tournaments.
 */
@Service
public class TournamentService {

    private final TournamentMongoRepository tournamentMongoRepository;
    private final GameMongoRepository gameMongoRepository;
    private final UserMongoRepository userMongoRepository;

    private final TournamentNeo4jRepository tournamentNeo4jRepository;
    private final GameNeo4jRepository gameNeo4jRepository;
    private final UserNeo4jRepository userNeo4jRepository;

    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);

    /**
     * Constructor for TournamentService.
     *
     * @param tournamentMongoRepository the tournament repository
     * @param gameRepository the game repository
     * @param userMongoRepository the user repository
     * @param tournamentNeo4jRepository the Neo4j tournament repository
     */
    public TournamentService(
            TournamentMongoRepository tournamentMongoRepository,
            GameMongoRepository gameRepository,
            UserMongoRepository userMongoRepository,
            TournamentNeo4jRepository tournamentNeo4jRepository,
            GameNeo4jRepository gameNeo4jRepository,
            UserNeo4jRepository userNeo4jRepository
    ) {
        this.tournamentMongoRepository = tournamentMongoRepository;
        this.gameMongoRepository = gameRepository;
        this.userMongoRepository = userMongoRepository;

        this.tournamentNeo4jRepository = tournamentNeo4jRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.userNeo4jRepository = userNeo4jRepository;
    }

    /**
     * Adds a new tournament.
     *
     * @param gameId the game ID
     * @param userIdMongo the user ID
     * @param addTournamentDTO the tournament creation DTO
     */
    public String addTournament (String gameId, String userIdMongo, AddTournamentDTO addTournamentDTO) {
        logger.info("Adding tournament for game with ID: {} and user with ID: {}", gameId, userIdMongo);
        logger.debug("Tournament creation DTO: {}", addTournamentDTO);

        // Retrieve the game from the database
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.warn("Game not found with ID: {}", gameId);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });
        GameNeo4j gameNeo4j = gameNeo4jRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.warn("Game not found with ID: {}", gameId);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });

        // Retrieve the user from the database
        User userMongo = userMongoRepository.findById(userIdMongo)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userIdMongo);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });
        UserNeo4j userNeo4j = userNeo4jRepository.findById(userIdMongo)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userIdMongo);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });

        // Create a new tournament
        Tournament tournament = new Tournament();
        tournament.setId(UUID.randomUUID().toString());
        tournament.setGame(new GameThread(game.getId(), game.getName(), game.getYearReleased()));
        tournament.setAdministrator(userMongo.getUsername());
        tournament.setName(addTournamentDTO.getName());
        tournament.setType(addTournamentDTO.getType());
        tournament.setTypeDescription(addTournamentDTO.getTypeDescription());
        tournament.setLocation(addTournamentDTO.getLocation());
        tournament.setNumParticipants(0);
        tournament.setMinParticipants(addTournamentDTO.getMinParticipants());
        tournament.setMaxParticipants(addTournamentDTO.getMaxParticipants());
        tournament.setVisibility(addTournamentDTO.getVisibility());
        tournament.setWinner(null);
        tournament.setOptions(addTournamentDTO.getOptions());
        tournament.setStartingTime(addTournamentDTO.getStartingTime());

        // If the tournament is public or invite-only, the list of allowed users is null
        logger.info("The tournament is " + addTournamentDTO.getVisibility());
        if (addTournamentDTO.getVisibility() == VisibilityTournament.PUBLIC || addTournamentDTO.getVisibility() == VisibilityTournament.INVITE) {
            tournament.setAllowed(null);
        }

        // If the tournament is private, the list of allowed users is not null
        else {
            logger.info("Retrieving allowed users");
            List<String> allowed = addTournamentDTO.getAllowed();

            // Retrieve from the `user` collection only the IDs that correspond to existing users
            List<String> existingIds = userMongoRepository.findUsersByUsername(allowed);

            // Find the IDs that do not exist by comparing them with the list of provided IDs
            List<String> nonExistingIds = allowed.stream()
                    .filter(userId -> existingIds.stream().noneMatch(existingId -> existingId.equals(userId)))
                    .toList();

            // If there are IDs that do not exist, throw an exception
            if (!nonExistingIds.isEmpty()) {
                logger.warn("The following user IDs do not exist: {}", nonExistingIds);
                throw new NotFoundException("The following user IDs do not exist: " + nonExistingIds);
            }

            tournament.setAllowed(existingIds);
        }

        // Increase tournaments created by the user by one
        userMongo.getTournaments().setCreated(userMongo.getTournaments().getCreated() + 1);

        // Write in Neo4j
        tournamentNeo4jRepository.save(
                userMongo.getUsername(),
                tournament.getId(),
                tournament.getName(),
                tournament.getVisibility().toString(),
                tournament.getMaxParticipants(),
                tournament.getStartingTime().toString(),
                game.getId()
        );

        // Writes in MongoDB
        userMongoRepository.save(userMongo);
        tournamentMongoRepository.save(tournament);

        logger.info("Tournament created successfully");
        return "Tournament successfully created!";
    }

    /**
     * Deletes a tournament.
     *
     * @param gameId the game ID
     * @param tournamentId the tournament ID
     * @param userId the user ID
     * @param tournamentsUser the tournaments user
     */
    public String deleteTournament(String gameId, String tournamentId, String userId, TournamentsUser tournamentsUser) {
        logger.info("Deleting tournament with ID: {}", tournamentId);

        // Retrieve the tournament from the database
        Tournament tournament = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found with ID: " + tournamentId));
        User user = userMongoRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Verify that the user is the administrator of the tournament or an admin
        if (!tournament.getAdministrator().equals(user.getUsername()) || !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("You are not the administrator of this tournament and you are not an admin");
        }
        tournamentMongoRepository.deleteById(tournamentId);

        // Decrease tournaments.created of the user by one
        user.getTournaments().setCreated(user.getTournaments().getCreated() - 1);

        userMongoRepository.save(user);

        //TODO RIMUOVERE ANCHE SU GRAPH

        return "Tournament successfully deleted!";
    }

    /**
     * Updates a tournament.
     *
     * @param gameId the game ID
     * @param tournamentId the tournament ID
     * @param username the username
     * @param updateTournDTO the tournament update DTO
     */
    public String updateTournament(String gameId, String tournamentId, String username, UpdateTournDTO updateTournDTO){
        Tournament tournament = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found with ID: " + tournamentId));

        // Verify that the user is the administrator of the tournament or an admin
        if (!tournament.getAdministrator().equals(username) || !userMongoRepository.findByUsername(username).get().getRole().equals(Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("You are not the administrator of this tournament and you are not an admin");
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

            // Increase tournaments.won of the indicated user by one
            User user = userMongoRepository.findByUsername(usernameWinner)
                    .orElseThrow(() -> new NotFoundException("User not found with username: " + usernameWinner));
            user.getTournaments().setWon(user.getTournaments().getWon() + 1);
            userMongoRepository.save(user);
        }
        if(updateTournDTO.getOptions() != null){
            tournament.setOptions(updateTournDTO.getOptions());
        }

        tournamentMongoRepository.save(tournament);

        //TODO MODIFICARE IL GRAFO

        return "Tournament successfully updated!";
    }

    /**
     * Gets tournaments for a game.
     *
     * @param gameId the game ID
     * @param userId the user ID
     * @param page the page number
     * @return a slice of tournament previews
     */
    public Slice<TournPreview> getTournaments(String gameId, String userId, int page) {
        return tournamentMongoRepository.findByGameOrderByStartingTimeDesc(gameId, userId, PageRequest.of(page, Constants.PAGE_SIZE));
    }

    /**
     * Gets a tournament by ID.
     *
     * @param tournamentId the tournament ID
     * @return the tournament DTO
     */
    public TournamentDTO getTournament(String tournamentId) {
        return tournamentMongoRepository.findById(tournamentId)
                .map(elem -> new TournamentDTO(elem.getName(), elem.getGame(), elem.getType(), elem.getTypeDescription(),
                        elem.getStartingTime(), elem.getLocation(), elem.getNumParticipants(), elem.getMinParticipants(),
                        elem.getMaxParticipants(), elem.getAdministrator(), elem.getWinner(), elem.getVisibility(), elem.getOptions()))
                .orElseThrow(() -> new NotFoundException("Tournament not found with ID: " + tournamentId));
    }
}