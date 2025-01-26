package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Game.*;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.repository.*;
import com.example.BoardVerse.utils.Constants;
import com.example.BoardVerse.utils.GameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.BoardVerse.utils.GameMapper.toGameMongo;

/**
 * Service class for managing games.
 */
@Service
public class GameService {

    private final GameMongoRepository gameMongoRepository;
    private final ReviewRepository reviewRepository;
    private final TournamentMongoRepository tournamentMongoRepository;
    private final ThreadRepository threadRepository;
    private final UserMongoRepository userMongoRepository;

    private final GameNeo4jRepository gameNeo4jRepository;

    static private final Logger logger = LoggerFactory.getLogger(GameService.class);

    /**
     * Constructor for GameService.
     *
     * @param gameMongoRepository the game repository
     * @param reviewRepository the review repository
     * @param tournamentMongoRepository the tournament repository
     * @param threadRepository the thread repository
     * @param userMongoRepository the user repository
     * @param userNeo4jRepository the Neo4j user repository
     * @param gameNeo4jRepository the Neo4j game repository
     * @param tournamentNeo4jRepository the Neo4j tournament repository
     */
    public GameService(
            GameMongoRepository gameMongoRepository,
            ReviewRepository reviewRepository,
            TournamentMongoRepository tournamentMongoRepository,
            ThreadRepository threadRepository,
            UserMongoRepository userMongoRepository,
            UserNeo4jRepository userNeo4jRepository,
            GameNeo4jRepository gameNeo4jRepository,
            TournamentNeo4jRepository tournamentNeo4jRepository
    ) {
        this.gameMongoRepository = gameMongoRepository;
        this.reviewRepository = reviewRepository;
        this.tournamentMongoRepository = tournamentMongoRepository;
        this.threadRepository = threadRepository;
        this.userMongoRepository = userMongoRepository;

        this.gameNeo4jRepository = gameNeo4jRepository;
    }

    /**
     * Adds a new game. TODO add consistency check
     *
     * @param newGameDTO the game creation DTO
     * @return a success message
     */
    public String addNewGame(GameCreationDTO newGameDTO) {
        logger.info("Adding new game: " + newGameDTO.getName());
        logger.debug("Game DTO: " + newGameDTO);

        // Check if a game with the same name and year released already exists
        if (gameMongoRepository.findByNameAndYearReleased(newGameDTO.getName(), newGameDTO.getYearReleased()).isPresent()) {
            logger.warn("Game " + newGameDTO.getName() + " released in " + newGameDTO.getYearReleased() + " already exists");
            throw new NotFoundException("Game " + newGameDTO.getName() + " released in " + newGameDTO.getYearReleased() + " already exists");
        }
        if(gameNeo4jRepository.findGameNeo4jByNameAndYearReleased(newGameDTO.getName(), newGameDTO.getYearReleased()).isPresent()){
            logger.warn("Game " + newGameDTO.getName() + " released in " + newGameDTO.getYearReleased() + " already exists");
            throw new NotFoundException("Game " + newGameDTO.getName() + " released in " + newGameDTO.getYearReleased() + " already exists");
        }

        // Generate a new game ID
        String gameId = UUID.randomUUID().toString();

        // Save the new game in Neo4j database
        GameNeo4j newGameNeo4j = GameMapper.toGameNeo4j(newGameDTO, gameId);
        gameNeo4jRepository.save(newGameNeo4j);
        logger.info("Game added to Neo4j");
        logger.debug("Game: " + newGameNeo4j);

        // Save the new game in Mongo database
        GameMongo newGameMongo = toGameMongo(newGameDTO, gameId);
        gameMongoRepository.save(newGameMongo);
        logger.info("Game added to MongoDB");
        logger.debug("Game: " + newGameMongo);

        return "Game " + newGameMongo.getName() + " released in " + newGameDTO.getYearReleased() + " added successfully";
    }

    /**
     * Updates an existing game.
     *
     * @param gameId the game ID
     * @param updateGameDTO the game update DTO
     * @return a success message
     */
    public String updateGame(String gameId, GameUpdateDTO updateGameDTO) {
        logger.info("Updating gameMongo: " + gameId);
        logger.debug("Update DTO: " + updateGameDTO);

        // Find the gameMongo to update
        GameMongo gameMongo = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.warn("Game not found with ID: " + gameId);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });
        GameNeo4j gameNeo4j = gameNeo4jRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.warn("Game not found with ID: " + gameId);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });
        logger.info("Game found");
        logger.debug("Game in mongo: " + gameMongo);
        logger.debug("Game in neo4j: " + gameNeo4j);


        if(updateGameDTO.getName()!=null){
            gameMongo.setName(updateGameDTO.getName());
            gameNeo4j.setName(updateGameDTO.getName());
        }

        if(updateGameDTO.getYearReleased()!=null){
            gameMongo.setYearReleased(updateGameDTO.getYearReleased());
            gameNeo4j.setYearReleased(updateGameDTO.getYearReleased());
        }

        if (updateGameDTO.getShortDescription()!=null) {
            gameMongo.setShortDescription(updateGameDTO.getShortDescription());
            gameNeo4j.setShortDescription(updateGameDTO.getShortDescription());
        }

        if(updateGameDTO.getCategories()!=null && !updateGameDTO.getCategories().isEmpty()) {
            gameMongo.setCategories(updateGameDTO.getCategories());
            gameNeo4j.setCategories(updateGameDTO.getCategories());
        }

        if(updateGameDTO.getDescription() != null){
            gameMongo.setDescription(updateGameDTO.getDescription());
        }

        if(updateGameDTO.getMinPlayers()!=null) {
            gameMongo.setMinPlayers(updateGameDTO.getMinPlayers());
        }

        if(updateGameDTO.getMaxPlayers()!=null) {
            gameMongo.setMaxPlayers(updateGameDTO.getMaxPlayers());
        }

        if(updateGameDTO.getMinSuggAge()!=null) {
            gameMongo.setMinSuggAge(updateGameDTO.getMinSuggAge());
        }

        if (updateGameDTO.getMinPlayTime()!=null) {
            gameMongo.setMinPlayTime(updateGameDTO.getMinPlayTime());
        }

        if (updateGameDTO.getMaxPlayTime()!=null) {
            gameMongo.setMaxPlayTime(updateGameDTO.getMaxPlayTime());
        }

        if (updateGameDTO.getDesigners()!=null) {
            gameMongo.setDesigners(updateGameDTO.getDesigners());
        }
        if (updateGameDTO.getArtists()!=null)
            gameMongo.setArtists(updateGameDTO.getArtists());

        if (updateGameDTO.getPublisher()!=null)
            gameMongo.setPublisher(updateGameDTO.getPublisher());

        if(updateGameDTO.getMechanics()!=null)
            gameMongo.setMechanics(updateGameDTO.getMechanics());

        if(updateGameDTO.getFamily()!=null)
            gameMongo.setFamily(updateGameDTO.getFamily());

        // Check if a game with the same name and year released already exists
        logger.info("Checking if game already exists");
        if(updateGameDTO.getName() != null || updateGameDTO.getYearReleased() != null) {
            // Check in mongo
            Optional<GameMongo> existingGameMongo = gameMongoRepository.findByNameAndYearReleased(gameMongo.getName(), gameMongo.getYearReleased());
            if (existingGameMongo.isPresent() && !existingGameMongo.get().getId().equals(gameId)) {
                logger.warn("Game " + updateGameDTO.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists in MongoDB");
                throw new NotFoundException("Game " + updateGameDTO.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists.");
            }

            // Check in neo4j
            Optional<GameNeo4j> existingGameNeo4j = gameNeo4jRepository.findGameNeo4jByNameAndYearReleased(gameNeo4j.getName(), gameNeo4j.getYearReleased());
            if (existingGameNeo4j.isPresent() && !existingGameNeo4j.get().getId().equals(gameId)) {
                logger.warn("Game " + updateGameDTO.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists in Neo4j");
                throw new NotFoundException("Game " + updateGameDTO.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists.");
            }
        }

        // If the name or year released has changed, update the info in the related entities
        if(updateGameDTO.getName() != null || updateGameDTO.getYearReleased() != null) {
            logger.info("Change in name or year released detected");

            // Update in Neo4j
            gameNeo4jRepository.save(gameNeo4j);
            logger.info("Game updated in Neo4j");

            // Threads
            threadRepository.updateGameInfoById(gameId, gameMongo.getName(), gameMongo.getYearReleased());
            // Tournaments
            tournamentMongoRepository.updateGameInfoById(gameId, gameMongo.getName(), gameMongo.getYearReleased());
            // Reviews
            reviewRepository.updateGameInfoById(gameId, gameMongo.getName(), gameMongo.getYearReleased(), gameMongo.getShortDescription());
            // Users' mostRecentReviews
            userMongoRepository.updateGameInfoById(gameId, gameMongo.getName(), gameMongo.getYearReleased(), gameMongo.getShortDescription());
            logger.info("Updated Game redundant info in MongoDB");
        }

        // If the short description has changed, update the info in the related entities
        else if (updateGameDTO.getShortDescription() != null) {
            logger.info("Change in short description detected");

            // Update in Neo4j
            gameNeo4jRepository.save(gameNeo4j);
            logger.info("Game updated in Neo4j");

            // Reviews
            reviewRepository.updateGameInfoById(gameId, gameMongo.getName(), gameMongo.getYearReleased(), gameMongo.getShortDescription());
            // Users' mostRecentReviews
            userMongoRepository.updateGameInfoById(gameId, gameMongo.getName(), gameMongo.getYearReleased(), gameMongo.getShortDescription());
            logger.info("Updated Game redundant info in MongoDB");
        }

        // If the categories have changed, update the info in the related entities
        else if (updateGameDTO.getCategories() != null) {
            logger.info("Change in categories detected");

            // Update in Neo4j
            gameNeo4jRepository.save(gameNeo4j);
            logger.info("Game updated in Neo4j");
        }

        // Save the updated game in MongoDB
        gameMongoRepository.save(gameMongo);
        logger.info("Game updated in MongoDB");

        return "Game " + gameMongo.getId() + " updated successfully";
    }

    /**
     * Deletes a game.
     *
     * @param gameId the game ID
     * @return a success message
     */
    public String deleteGame(String gameId) {
        logger.info("Deleting game with ID: " + gameId);

        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.warn("Game not found with ID: " + gameId);
                    return new NotFoundException("Game not found with ID: " + gameId);
                });

        // Delete from Neo4j
        gameNeo4jRepository.deleteById(gameId);
        logger.info("Game deleted from Neo4j");

        // Delete threads
        threadRepository.deleteAllByGameId(gameId);
        // Delete reviews
        reviewRepository.deleteByGameId(gameId);
        // Delete tournaments
        tournamentMongoRepository.deleteByGameId(gameId);
        // Delete users' mostRecentReviews
        userMongoRepository.deleteGameFromMostRecentReviews(gameId);
        logger.info("Game redundant info deleted from MongoDB");

        // Delete from MongoDB
        gameMongoRepository.delete(game);
        logger.info("Game deleted from MongoDB");

        return "Game with id " + gameId + " deleted successfully";
    }

    /**
     * Finds games by name.
     *
     * @param name the game name
     * @param page the page number
     * @return a slice of game previews
     */
    public Slice<GameRankPreviewDTO> findByName(String name, int page) {
        logger.info("Finding games with name: " + name);
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return gameMongoRepository.findByNameContaining(name, pageable);
    }

    /**
     * Gets game information.
     *
     * @param gameId the game ID
     * @return the game information DTO
     */
    public GameInfoDTO getInfo(String gameId) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.warn("Game not found with ID: " + gameId);
                    return new NotFoundException("Game not found with ID: " + gameId)
                });
        return GameMapper.toDTO(game);
    }

    /**
     * Gets rating details for a game.
     *
     * @param gameId the game ID
     * @return the rating details
     */
    public RatingDetails getRatingsDetails(String gameId) {
        logger.info("Getting rating details for game with ID: " + gameId);
        return reviewRepository.findRatingDetailsByGameId(gameId);
    }

    /**
     * Gets filtered games.
     *
     * @param yearReleased the release year
     * @param categories the categories
     * @param mechanics the mechanics
     * @param sortBy the sort field
     * @param order the sort order
     * @param page the page number
     * @return a slice of game previews
     */
    public Slice<GameRankPreviewDTO> getFilteredGames(Integer yearReleased, String categories,
                                                      String mechanics, String sortBy, String order, int page) {
        logger.info("Getting filtered games");
        logger.debug("Year released: " + yearReleased);
        logger.debug("Categories: " + categories);
        logger.debug("Mechanics: " + mechanics);
        logger.debug("Sort by: " + sortBy);
        logger.debug("Order: " + order);
        logger.debug("Page: " + page);

        // Determine the field to sort by
        Sort sort;
        if ("yearReleased".equalsIgnoreCase(sortBy)) {
            // Consider asc/desc
            sort = "asc".equalsIgnoreCase(order)
                    ? Sort.by("yearReleased").ascending()
                    : Sort.by("yearReleased").descending();
        } else {
            // Default to sorting by averageRating
            sort = "asc".equalsIgnoreCase(order)
                    ? Sort.by("averageRating").ascending()
                    : Sort.by("averageRating").descending();
        }

        // 10 items per page.
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE, sort);

        return gameMongoRepository.findGamesByFilters(
                yearReleased, categories, mechanics, pageable
        );
    }

    /**
     * Gets the ranking of games.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param country the country
     * @param state the state
     * @param city the city
     * @param page the page number
     * @return a slice of game previews
     */
    public Slice<GameRankPreviewDTO> getRanking(Date startDate, Date endDate, String country, String state, String city, int page) {
        logger.info("Getting ranking of games");
        logger.debug("Start date: " + startDate);
        logger.debug("End date: " + endDate);
        logger.debug("Country: " + country);
        logger.debug("State: " + state);
        logger.debug("City: " + city);
        logger.debug("Page: " + page);

        if(startDate == null){
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }

        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return reviewRepository.findAverageRatingByPostDateLocation(startDate, endDate, country, state, city, pageable);
    }

    /**
     * Gets the most played games.
     *
     * @return a list of most played game DTOs
     */
    public List<MostPlayedGameDTO> getMostPlayedGames() {
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date();
        calendar.add(Calendar.YEAR, -1);
        Date startDate = calendar.getTime();

        return tournamentMongoRepository.findTop10GamesWithHighestAverageParticipation(startDate, endDate);
    }

    /**
     * Gets the best games by age.
     *
     * @return a list of best game age DTOs
     */
    public List<BestGameAgeDTO> bestGamesByAge() {
        logger.info("Getting best games by age");
        return reviewRepository.findBestGameByAgeBrackets();
    }

    /**
     * Gets game stats about tournaments and likes.
     *
     * @return a list of best game player DTOs
     */
    public List<GameAnalyticsDTO> getGameAnalytics(String gameId) {
        logger.info("Getting game analytics for game with ID: " + gameId);
        return gameNeo4jRepository.getGameAnalytics(gameId);
    }

}