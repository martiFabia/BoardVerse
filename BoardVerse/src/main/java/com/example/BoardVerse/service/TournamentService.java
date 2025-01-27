package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Tournament.*;
import com.example.BoardVerse.DTO.User.TournamentParticipantDTO;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.MongoDB.TournamentMongo;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.model.MongoDB.subentities.Role;
import com.example.BoardVerse.model.MongoDB.subentities.TournamentsUser;
import com.example.BoardVerse.model.MongoDB.subentities.VisibilityTournament;
import com.example.BoardVerse.repository.*;
import com.example.BoardVerse.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        logger.info("Adding tournament for game with ID: {} and user with ID: {}", gameId, userIdMongo);
        logger.debug("Tournament creation DTO: {}", addTournamentDTO);

        // Tournament cannot be started in the past
        if (!addTournamentDTO.getStartingTime().after(new Date())) {
            logger.warn("The tournament cannot be already started");
            throw new IllegalArgumentException("The tournament cannot be already started");
        }

        // Retrieve the game from the database
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.warn("Game not found with ID: {}", gameId);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });
        if(gameNeo4jRepository.findById(gameId).isEmpty()) {
            logger.warn("Game not found with ID: {}", gameId);
            throw new NotFoundException("Game not found with ID: " + gameId);
        }

        // Retrieve the user from the database
        UserMongo userMongo = userMongoRepository.findById(userIdMongo)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userIdMongo);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });
        if(userNeo4jRepository.findById(userIdMongo).isEmpty()) {
            logger.warn("User not found with ID: {}", userIdMongo);
            throw new NotFoundException("User not found with ID: " + userIdMongo);
        }

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

        logger.info("Tournament created successfully");
        return "Tournament successfully created!";
    }

    /**
     * Deletes a tournament.
     *
     * @param tournamentId the tournament ID
     * @param userId the user ID
     */
    public String deleteTournament(String tournamentId, String userId) {
        logger.info("Deleting tournament with ID: {}", tournamentId);

        // Check if tournament exists
        TournamentMongo tournamentMongo = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found in MongoDB with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });
        logger.debug("Tournament found in Mongo: {}", tournamentMongo);
        ExtendedTournamentNeo4jInfo extendedTournamentNeo4jInfo = tournamentNeo4jRepository.extendedFindById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found in Neo4j with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });
        logger.debug("Tournament found in Neo4j: {}", extendedTournamentNeo4jInfo);

        // Check if user exists
        UserMongo userMongo = userMongoRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found in MongoDB with ID: {}", userId);
                    return new NotFoundException("User not found with ID: " + userId);
                });
        if (userNeo4jRepository.findById(userId).isEmpty()) {
            logger.warn("User not found in Neo4j with ID: {}", userId);
            throw new NotFoundException("User not found with ID: " + userId);
        }

        // Verify that the userMongo is the administrator of the tournamentMongo or an admin
        if (!tournamentMongo.getAdministrator().equals(userMongo.getUsername()) || !userMongo.getRole().equals(Role.ROLE_ADMIN)) {
            logger.warn("ACCESS DENIED! User" + userMongo.getUsername() + " is not the administrator of this tournament neither an admin");
            throw new AccessDeniedException("You are not the administrator of this tournament neither an admin");
        }

        // Delete tournament from Neo4j
        tournamentNeo4jRepository.deleteById(tournamentId);
        logger.info("Tournament successfully deleted from Neo4j");

        // If there's no winner (so the tournament is not concluded), update participants and administrator statistics
        if (tournamentMongo.getWinner() == null) {
            tournamentMongoRepository.decrementTournamentStats(extendedTournamentNeo4jInfo.participants(), tournamentMongo.getAdministrator());
            logger.info("Tournament stats decremented");
        }

        // Delete tournament from MongoDB
        tournamentMongoRepository.deleteById(tournamentId);
        logger.info("Tournament successfully deleted from MongoDB");

        return "Tournament successfully deleted!";
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
        logger.info("Updating tournament with ID: {}", tournamentId);

        // Check if tournament exists
        TournamentMongo tournamentMongo = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });
        ExtendedTournamentNeo4jInfo extendedTournamentNeo4jInfo = tournamentNeo4jRepository.extendedFindById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });


        // Verify that the user is the administrator of the tournamentMongo or an admin
        if (!tournamentMongo.getAdministrator().equals(username) || !userMongoRepository.findByUsername(username).get().getRole().equals(Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("You are not the administrator of this tournament and you are not an admin");
        }

        String initialTournamentWinner = tournamentMongo.getWinner();
        VisibilityTournament initialTournamentVisibility = tournamentMongo.getVisibility();

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
            tournamentMongo.setWinner(updateTournamentDTO.getWinner());
        }
        if(updateTournamentDTO.getOptions() != null){
            tournamentMongo.setOptions(updateTournamentDTO.getOptions());
        }

        if (updateTournamentDTO.getWinner() != null && userMongoRepository.findByUsername(updateTournamentDTO.getWinner()).isEmpty()) {
            logger.warn("User not found with username: {}", updateTournamentDTO.getWinner());
            throw new NotFoundException("User not found with username: " + updateTournamentDTO.getWinner());
        }

        // The tournament cannot have a winner if it has not started yet
        if (!tournamentMongo.getStartingTime().before(new Date()) && tournamentMongo.getWinner() != null) {
            logger.warn("The tournament has not started yet, it cannot have a winner");
            throw new AccessDeniedException("The tournament has not started yet, it cannot have a winner");
        }

        // Check if the winner is a participant
        if (updateTournamentDTO.getWinner() != null && !extendedTournamentNeo4jInfo.participants().contains(updateTournamentDTO.getWinner())) {
            logger.warn("The winner is not a participant");
            throw new NotFoundException("The winner is not a participant");
        }

        // Maximum number of participants cannot be reduced below the current number of participants
        if (updateTournamentDTO.getMaxParticipants() != null && updateTournamentDTO.getMaxParticipants() < extendedTournamentNeo4jInfo.participants().size()) {
            logger.warn("Maximum number of participants cannot be reduced below the current number of participants");
            throw new IllegalArgumentException("Maximum number of participants cannot be reduced below the current number of participants");
        }

        // Visibility cannot be changed from / to private
        if (updateTournamentDTO.getVisibility() != null && (initialTournamentVisibility.equals(VisibilityTournament.PRIVATE) || updateTournamentDTO.getVisibility().equals(VisibilityTournament.PRIVATE))) {
            logger.warn("Visibility cannot be changed from / to private");
            throw new IllegalArgumentException("Visibility cannot be changed from / to private");
        }

        // Update Neo4j
        tournamentNeo4jRepository.updateTournament(
                tournamentId,
                updateTournamentDTO.getName(),
                updateTournamentDTO.getVisibility().toString(),
                updateTournamentDTO.getMaxParticipants(),
                updateTournamentDTO.getStartingTime().toString(),
                updateTournamentDTO.getWinner()
        );
        logger.info("Tournament successfully updated in Neo4j");

        // Update old winner statistics
        if (initialTournamentWinner != null) {
            Optional<UserMongo> initialWinner = userMongoRepository.findByUsername(initialTournamentWinner);
            // It could be that the user has been deleted
            if (initialWinner.isPresent()) {
                initialWinner.get().getTournaments().setWon(initialWinner.get().getTournaments().getWon() - 1);
                userMongoRepository.save(initialWinner.get());
                logger.info("Old winner statistics updated");
            }
        }

        // Update new winner statistics
        if (tournamentMongo.getWinner() != null) {
            UserMongo winner = userMongoRepository.findByUsername(tournamentMongo.getWinner()).get();
            winner.getTournaments().setWon(winner.getTournaments().getWon() + 1);
            userMongoRepository.save(winner);
            logger.info("New winner statistics updated");
        }

        // Update MongoDB tournament
        tournamentMongoRepository.save(tournamentMongo);
        logger.info("Tournament successfully updated in MongoDB");

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
        logger.info("Retrieving tournaments for game with ID: {}", gameId);
        return tournamentMongoRepository.findByGameOrderByStartingTimeDesc(gameId, userId, PageRequest.of(page, Constants.PAGE_SIZE));
    }

    /**
     * Gets a tournament by ID.
     *
     * @param tournamentId the tournament ID
     * @return the tournament DTO
     */
    public TournamentDTO getTournament(String tournamentId) {
        logger.info("Retrieving tournament with ID: {}", tournamentId);
        return tournamentMongoRepository.findById(tournamentId)
                .map(elem -> new TournamentDTO(elem.getName(), elem.getGame(), elem.getType(), elem.getTypeDescription(),
                        elem.getStartingTime(), elem.getLocation(), elem.getNumParticipants(), elem.getMinParticipants(),
                        elem.getMaxParticipants(), elem.getAdministrator(), elem.getWinner(), elem.getVisibility(), elem.getOptions()))
                .orElseThrow(() -> new NotFoundException("Tournament not found with ID: " + tournamentId));
    }

    /**
     * Gets the participants of a tournament.
     *
     * @param tournamentId the tournament ID
     * @param SortBy the sorting criteria
     * @param page the page number
     * @return a list of tournament participant DTOs
     */
    public List<TournamentParticipantDTO> getTournamentParticipants(String tournamentId, String SortBy, int page, int pageSize) {
        logger.info("Retrieving participants for tournament with ID: {}", tournamentId);
        return tournamentNeo4jRepository.getParticipants(tournamentId, SortBy, page, pageSize);
    }

    /**
     * Registers a user for a tournament.
     *
     * @param tournamentId the tournament ID
     * @param userId the user ID
     * @return a message
     */
    public String registerToTournament(String tournamentId, String userId) {
        logger.info("User with ID: {} is participating to tournament with ID: {}", userId, tournamentId);

        // Check if the user exists
        UserMongo userMongo = userMongoRepository.findByUsername(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found with ID: " + userId);
                });
        if (userNeo4jRepository.findById(userId).isEmpty()) {
            logger.warn("User not found with ID: {}", userId);
            throw new NotFoundException("User not found with ID: " + userId);
        }

        // Check if the tournament exists
        TournamentMongo tournamentMongo = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });
        ExtendedTournamentNeo4jInfo extendedTournamentNeo4jInfo = tournamentNeo4jRepository.extendedFindById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });

        // Check if tournament is full
        if (tournamentMongo.getNumParticipants() >= tournamentMongo.getMaxParticipants()) {
            logger.warn("Tournament is full");
            throw new AccessDeniedException("Tournament is full");
        }
        // Check if user is already participating
        if (extendedTournamentNeo4jInfo.participants().contains(userId)) {
            logger.warn("User is already participating to the tournament");
            throw new AccessDeniedException("User is already participating to the tournament");
        }
        // Check if the tournament has already started
        if (tournamentMongo.getStartingTime().before(new Date())) {
            logger.warn("Tournament has already started");
            throw new AccessDeniedException("Tournament has already started");
        }

        // Add user to the tournament
        tournamentNeo4jRepository.addParticipantToTournament(tournamentId, userId);
        logger.info("User successfully added to the tournament in Neo4j");

        // Increment user statistics
        userMongo.getTournaments().setPartecipated(userMongo.getTournaments().getPartecipated() + 1);
        userMongoRepository.save(userMongo);
        logger.info("User statistics successfully updated");

        // Increment the number of participants
        tournamentMongo.setNumParticipants(tournamentMongo.getNumParticipants() + 1);
        tournamentMongoRepository.save(tournamentMongo);
        logger.info("Tournament statistics successfully updated");

        return "User successfully participated to the tournament!";
    }

    /**
     * Unregisters a user from a tournament.
     *
     * @param tournamentId the tournament ID
     * @param userId the user ID
     * @return a message
     */
    public String unregisterFromTournament(String tournamentId, String userId) {
        logger.info("User with ID: {} is no longer participating to tournament with ID: {}", userId, tournamentId);

        // Check if the user exists
        UserMongo userMongo = userMongoRepository.findByUsername(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found with ID: " + userId);
                });
        if (userNeo4jRepository.findById(userId).isEmpty()) {
            logger.warn("User not found with ID: {}", userId);
            throw new NotFoundException("User not found with ID: " + userId);
        }

        // Check if the tournament exists
        TournamentMongo tournamentMongo = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });
        ExtendedTournamentNeo4jInfo extendedTournamentNeo4jInfo = tournamentNeo4jRepository.extendedFindById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });

        // Check if user is participating
        if (!extendedTournamentNeo4jInfo.participants().contains(userId)) {
            logger.warn("User is not participating to the tournament");
            throw new AccessDeniedException("User is not participating to the tournament");
        }
        // Check if the tournament has already started
        if (tournamentMongo.getStartingTime().before(new Date())) {
            logger.warn("Tournament has already started");
            throw new AccessDeniedException("Tournament has already started");
        }

        // Remove user from the tournament
        tournamentNeo4jRepository.removeParticipantFromTournament(userId, tournamentId);
        logger.info("User successfully removed from the tournament in Neo4j");

        // Decrement user statistics
        userMongo.getTournaments().setPartecipated(userMongo.getTournaments().getPartecipated() - 1);
        userMongoRepository.save(userMongo);
        logger.info("User statistics successfully updated");

        // Decrement the number of participants
        tournamentMongo.setNumParticipants(tournamentMongo.getNumParticipants() - 1);
        tournamentMongoRepository.save(tournamentMongo);

        return "User successfully unregistered from the tournament!";

    }

    public String selectWinner(String tournamentId, String winnerUsername, String username) {
        logger.info("Adding winner with ID: {} to tournament with ID: {}", winnerUsername, tournamentId);

        // Check if the winner exists
        UserMongo userMongo = userMongoRepository.findByUsername(winnerUsername)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", winnerUsername);
                    return new NotFoundException("User not found with ID: " + winnerUsername);
                });
        if (userNeo4jRepository.findById(winnerUsername).isEmpty()) {
            logger.warn("User not found with ID: {}", winnerUsername);
            throw new NotFoundException("User not found with ID: " + winnerUsername);
        }

        // Check if the tournament exists
        TournamentMongo tournamentMongo = tournamentMongoRepository.findById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });
        ExtendedTournamentNeo4jInfo extendedTournamentNeo4jInfo = tournamentNeo4jRepository.extendedFindById(tournamentId)
                .orElseThrow(() -> {
                    logger.warn("Tournament not found with ID: {}", tournamentId);
                    return new NotFoundException("Tournament not found with ID: " + tournamentId);
                });

        // Check if the user is the administrator of the tournament
        if (!tournamentMongo.getAdministrator().equals(username)) {
            logger.warn("User is not the administrator of the tournament");
            throw new AccessDeniedException("You are not the administrator of the tournament");
        }

        // Check if the user is a participant
        if (!extendedTournamentNeo4jInfo.participants().contains(winnerUsername)) {
            logger.warn("User is not a participant to the tournament");
            throw new AccessDeniedException("User is not a participant to the tournament");
        }
        // Check if the tournament has already started
        if (tournamentMongo.getStartingTime().after(new Date())) {
            logger.warn("Tournament has not started yet");
            throw new AccessDeniedException("Tournament has not started yet");
        }

        // Check if the user is already the winner
        if (tournamentMongo.getWinner() != null) {
            logger.warn("User is already the winner of the tournament");
            throw new AccessDeniedException("User is already the winner of the tournament");
        }

        // Add the winner to the tournament (Neo4j)
        tournamentNeo4jRepository.setWinner(tournamentId, winnerUsername);
        logger.info("Winner successfully added to the tournament in Neo4j");

        // Add the winner to the tournament
        tournamentMongo.setWinner(winnerUsername);
        tournamentMongoRepository.save(tournamentMongo);
        logger.info("Winner successfully added to the tournament in MongoDB");

        // Increment user statistics
        userMongo.getTournaments().setWon(userMongo.getTournaments().getWon() + 1);
        userMongoRepository.save(userMongo);
        logger.info("User statistics successfully updated");

        return "Winner successfully added to the tournament!";
    }


    /**
     * Gets the difficulty of a tournament.
     *
     * @param tournamentId the tournament ID
     * @return the difficulty of the tournament
     */
    public double getTournamentDifficultyIndex(String tournamentId) {
        logger.info("Retrieving difficulty for tournament with ID: {}", tournamentId);
        return tournamentNeo4jRepository.getTournamentDifficulty(tournamentId);
    }

    /**
     * Gets how much connected the participants of a tournament are to each other.
     *
     * @param tournamentId the tournament ID
     * @return the social density of the tournament
     */
    public double getTournamentSocialDensityIndex(String tournamentId) {
        return tournamentNeo4jRepository.getTournamentSocialDensity(tournamentId);
    }
}