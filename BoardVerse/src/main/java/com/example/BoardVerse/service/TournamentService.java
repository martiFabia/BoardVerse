package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Tournament.AddTournamentDTO;
import com.example.BoardVerse.DTO.Tournament.TournPreview;
import com.example.BoardVerse.DTO.Tournament.TournamentDTO;
import com.example.BoardVerse.DTO.Tournament.UpdateTournamentDTO;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.TournamentMongo;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.model.MongoDB.subentities.Role;
import com.example.BoardVerse.model.MongoDB.subentities.TournamentsUser;
import com.example.BoardVerse.model.MongoDB.subentities.VisibilityTournament;
import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.model.Neo4j.TournamentNeo4j;
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

import static com.example.BoardVerse.utils.GameMapper.toPreviewEssential;
import static com.example.BoardVerse.utils.TournamentMapper.toTournamentMongo;

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
        logger.info("Adding tournamentMongo for game with ID: {} and user with ID: {}", gameId, userIdMongo);
        logger.debug("TournamentMongo creation DTO: {}", addTournamentDTO);

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
        UserMongo userMongo = userMongoRepository.findById(userIdMongo)
                .orElseThrow(() -> {
                    logger.warn("UserMongo not found with ID: {}", userIdMongo);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });
        UserNeo4j userNeo4j = userNeo4jRepository.findById(userIdMongo)
                .orElseThrow(() -> {
                    logger.warn("UserMongo not found with ID: {}", userIdMongo);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });

        // Create a new tournamentMongo
        TournamentMongo tournamentMongo = toTournamentMongo(
                addTournamentDTO,
                UUID.randomUUID().toString(),
                toPreviewEssential(game),
                userMongo
        );

        // If the tournamentMongo is private, the list of allowed users is not null
        if (tournamentMongo.getVisibility().equals(VisibilityTournament.PRIVATE)) {
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

            tournamentMongo.setAllowed(existingIds);
        }

        // Increase tournaments created by the user by one
        userMongo.getTournaments().setCreated(userMongo.getTournaments().getCreated() + 1);

        // Write in Neo4j
        tournamentNeo4jRepository.save(
                userMongo.getUsername(),
                tournamentMongo.getId(),
                tournamentMongo.getName(),
                tournamentMongo.getVisibility().toString(),
                tournamentMongo.getMaxParticipants(),
                tournamentMongo.getStartingTime().toString(),
                game.getId()
        );

        // Writes in MongoDB
        userMongoRepository.save(userMongo);
        tournamentMongoRepository.save(tournamentMongo);

        logger.info("TournamentMongo created successfully");
        return "TournamentMongo successfully created!";
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
        logger.info("Deleting tournamentMongo with ID: {}", tournamentId);

        // Check if tournament exists
        TournamentMongo tournamentMongo = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("TournamentMongo not found with ID: {}", tournamentId);
                    return new NotFoundException("TournamentMongo not found with ID: " + tournamentId);
                });
        TournamentNeo4j tournamentNeo4j = tournamentNeo4jRepository.findById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("TournamentNeo4j not found with ID: {}", tournamentId);
                    return new NotFoundException("TournamentMongo not found with ID: " + tournamentId);
                });

        // Check if user exists
        UserMongo userMongo = userMongoRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("UserMongo not found with ID: {}", userId);
                    return new NotFoundException("UserMongo not found with ID: " + userId);
                });
        UserNeo4j userNeo4j = userNeo4jRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("UserNeo4j not found with ID: {}", userId);
                    return new NotFoundException("UserMongo not found with ID: " + userId);
                });

        // Verify that the userMongo is the administrator of the tournamentMongo or an admin
        if (!tournamentMongo.getAdministrator().equals(userMongo.getUsername()) || !userMongo.getRole().equals(Role.ROLE_ADMIN)) {
            logger.warn("ACCESS DENIED! User" + userMongo.getUsername() + " is not the administrator of this tournamentMongo neither an admin");
            throw new AccessDeniedException("You are not the administrator of this tournamentMongo neither an admin");
        }





        tournamentMongoRepository.deleteById(tournamentId);


        userMongo.getTournaments().setCreated(userMongo.getTournaments().getCreated() - 1);

        userMongoRepository.save(userMongo);


        return "TournamentMongo successfully deleted!";
    }

    /**
     * Updates a tournament.
     *
     * @param gameId the game ID
     * @param tournamentId the tournament ID
     * @param username the username
     * @param updateTournamentDTO the tournament update DTO
     */
    public String updateTournament(String gameId, String tournamentId, String username, UpdateTournamentDTO updateTournamentDTO){
        TournamentMongo tournamentMongo = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("TournamentMongo not found with ID: " + tournamentId));

        // Verify that the user is the administrator of the tournamentMongo or an admin
        if (!tournamentMongo.getAdministrator().equals(username) || !userMongoRepository.findByUsername(username).get().getRole().equals(Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("You are not the administrator of this tournamentMongo and you are not an admin");
        }

        if(updateTournamentDTO.getName() != null){
            tournamentMongo.setName(updateTournamentDTO.getName());
        }
        if(updateTournamentDTO.getType() != null){
            tournamentMongo.setType(updateTournamentDTO.getType());
        }
        if(updateTournamentDTO.getTypeDescription() != null){
            tournamentMongo.setTypeDescription(updateTournamentDTO.getTypeDescription());
        }
        if(updateTournamentDTO.getStartingTime() != null){
            tournamentMongo.setStartingTime(updateTournamentDTO.getStartingTime());
        }
        if(updateTournamentDTO.getLocation() != null){
            tournamentMongo.setLocation(updateTournamentDTO.getLocation());
        }
        if(updateTournamentDTO.getMinParticipants() != null){
            tournamentMongo.setMinParticipants(updateTournamentDTO.getMinParticipants());
        }
        if(updateTournamentDTO.getMaxParticipants() != null){
            tournamentMongo.setMaxParticipants(updateTournamentDTO.getMaxParticipants());
        }
        if(updateTournamentDTO.getVisibility() != null){
            tournamentMongo.setVisibility(updateTournamentDTO.getVisibility());
        }
        if(updateTournamentDTO.getWinner() != null){
            String usernameWinner = updateTournamentDTO.getWinner();
            tournamentMongo.setWinner(usernameWinner);

            // Increase tournaments.won of the indicated userMongo by one
            UserMongo userMongo = userMongoRepository.findByUsername(usernameWinner)
                    .orElseThrow(() -> new NotFoundException("UserMongo not found with username: " + usernameWinner));
            userMongo.getTournaments().setWon(userMongo.getTournaments().getWon() + 1);
            userMongoRepository.save(userMongo);
        }
        if(updateTournamentDTO.getOptions() != null){
            tournamentMongo.setOptions(updateTournamentDTO.getOptions());
        }

        tournamentMongoRepository.save(tournamentMongo);

        //TODO MODIFICARE IL GRAFO

        return "TournamentMongo successfully updated!";
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
                .orElseThrow(() -> new NotFoundException("TournamentMongo not found with ID: " + tournamentId));
    }
}