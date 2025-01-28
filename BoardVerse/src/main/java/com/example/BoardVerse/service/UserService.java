package com.example.BoardVerse.service;


import com.example.BoardVerse.DTO.Game.GameLikedDTO;
import com.example.BoardVerse.DTO.Review.ReviewUserDTO;
import com.example.BoardVerse.DTO.Tournament.TournamentNeo4jDTO;
import com.example.BoardVerse.DTO.User.*;
import com.example.BoardVerse.DTO.User.activity.*;
import com.example.BoardVerse.exception.AlreadyExistsException;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.model.Neo4j.GameNeo4j;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import com.example.BoardVerse.repository.*;
import com.example.BoardVerse.security.jwt.JwtUtils;
import com.example.BoardVerse.utils.Constants;
import com.example.BoardVerse.utils.UserMapper;
import org.neo4j.cypherdsl.core.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private AuthService authService;

    @Autowired
    private final UserMongoRepository userMongoRepository;
    private final ReviewRepository reviewRepository;
    private final ThreadRepository threadRepository;
    @Autowired
    private TournamentMongoRepository tournamentMongoRepository;
    private final GameMongoRepository gameMongoRepository;

    private final UserNeo4jRepository userNeo4jRepository;
    private final GameNeo4jRepository gameNeo4jRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Constructor for UserService.
     *
     * @param authManager the authentication manager
     * @param jwtUtils the JWT utility
     * @param userMongoRepository the user repository
     * @param reviewRepository the review repository
     * @param threadRepository the thread repository
     * @param tournamentMongoRepository the tournament repository
     */
    @Autowired
    public UserService(
            AuthenticationManager authManager,
            JwtUtils jwtUtils,
            UserMongoRepository userMongoRepository,
            ReviewRepository reviewRepository,
            ThreadRepository threadRepository,
            TournamentMongoRepository tournamentMongoRepository,
            GameMongoRepository gameMongoRepository,
            UserNeo4jRepository userNeo4jRepository,
            GameNeo4jRepository gameNeo4jRepository
    ) {
        this.authenticationManager = authManager;
        this.jwtUtils = jwtUtils;

        this.userMongoRepository = userMongoRepository;
        this.reviewRepository = reviewRepository;
        this.threadRepository = threadRepository;
        this.tournamentMongoRepository = tournamentMongoRepository;
        this.gameMongoRepository = gameMongoRepository;

        this.gameNeo4jRepository = gameNeo4jRepository;
        this.userNeo4jRepository = userNeo4jRepository;
    }

    /**
     * Retrieves a slice of users by username.
     *
     * @param username the username to search for
     * @param page the page number
     * @return a slice of user DTOs
     */
    public Slice<UserDTO> getUserByUsername(String username, int page) {
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return userMongoRepository.findByUsernameContaining(username, pageable);
    }

    /**
     * Retrieves user information by username.
     *
     * @param username the username to search for
     * @return the user information DTO
     */
    public UserInfoDTO getInfo(String username) {
        Optional<UserMongo> user = userMongoRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new NotFoundException("User not found with username: " + username);
        }
        return UserMapper.toInfoDTO(user.get());
    }

    /**
     * Updates a user with the given updates.
     *
     * @param userId the user ID
     * @param updates the user update DTO
     * @return the updated user information DTO
     */
    @Retryable(
            retryFor = {DataAccessException.class, TransactionSystemException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public UserInfoDTO updateUser(String userId, UserUpdateDTO updates) {
        logger.info("Updating user with id: " + userId);

        // Check if user exists
        UserMongo userMongo = userMongoRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with id: " + userId);
                    return new NotFoundException("User not found with id: " + userId);
                });
        UserNeo4j userNeo4j = userNeo4jRepository.findByUsername(userMongo.getUsername())
                .orElseThrow(() -> {
                    logger.warn("UserNeo4j not found with username: " + userMongo.getUsername());
                    return new NotFoundException("UserNeo4j not found with username: " + userMongo.getUsername());
                });

        String initialUsername = userMongo.getUsername();

        if (updates.username() != null) {
            if (userMongoRepository.existsByUsername(updates.username())) {
                throw new AlreadyExistsException("Username already exists");
            }
            userMongo.setUsername(updates.username());
            userNeo4j.setUsername(updates.username());
        }

        if (updates.password() != null) {
            userMongo.setPassword(encoder.encode(updates.password()));
        }

        if (updates.email() != null) {
            if (userMongoRepository.existsByEmail(updates.email())) {
                throw new AlreadyExistsException("Email already exists");
            }
            userMongo.setEmail(updates.email());
        }

        if(updates.firstName() != null) {
            userMongo.setFirstName(updates.firstName());
        }

        if (updates.lastName() != null) {
            userMongo.setLastName(updates.lastName());
        }

        if (updates.birthDate() != null) {
            userMongo.setBirthDate(updates.birthDate());
        }

        if(updates.location() != null) {
            if (userMongo.getLocation() == null) {
                userMongo.setLocation(new Location());
            }
            if (updates.location().getCountry() != null) {
                userMongo.getLocation().setCountry(updates.location().getCountry());
            }
            if (updates.location().getStateOrProvince() != null) {
                userMongo.getLocation().setStateOrProvince(updates.location().getStateOrProvince());
            }
            if (updates.location().getCity() != null) {
                userMongo.getLocation().setCity(updates.location().getCity());
            }
        }

        // Update user in Neo4j
        userNeo4jRepository.save(userNeo4j);
        logger.info("UserNeo4j updated successfully");

        // Update redundancies
        if (!initialUsername.equals(userMongo.getUsername())) {
            // Reviews
            reviewRepository.updateUsernameInReviews(userMongo.getUsername(), updates.username());
            // Threads
            threadRepository.updateThreadAuthorUsername(userMongo.getUsername(), updates.username());
            // Messages
            threadRepository.updateMessageAuthorUsername(userMongo.getUsername(), updates.username());
            // Replies
            threadRepository.updateReplyToUsername(userMongo.getUsername(), updates.username());
            // Tournaments
            tournamentMongoRepository.updateUsernameInTournaments(userMongo.getUsername(), updates.username());
        }
        logger.info("Redundancies updated successfully");

        userMongoRepository.save(userMongo);
        logger.info("User updated successfully");

        return UserMapper.toInfoDTO(userMongo.get());
    }

    /**
     * Deletes a user by username.
     *
     * @param username the username to delete
     * @return a message indicating successful deletion
     */
    @Retryable(
            retryFor = {DataAccessException.class, TransactionSystemException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String deleteUser(String username) {
        logger.info("Deleting user with username: " + username);

        // Check if user exists
        UserMongo userMongo = userMongoRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: " + username);
                    return new NotFoundException("User not found with username: " + username);
                });

        // Delete user in Neo4j
        if(!userNeo4jRepository.deleteByUsername(username)){
            logger.warn("UserNeo4j not found with username: " + username);
            throw new NotFoundException("UserNeo4j not found with username: " + username);
        }
        logger.info("UserNeo4j deleted successfully");

        // Delete redundancies in MongoDB
        // Reviews
        reviewRepository.updateUsernameInReviews(username, null);
        // Threads
        threadRepository.updateThreadAuthorUsername(username, null);
        // Messages
        threadRepository.updateMessageAuthorUsername(username, null);
        // Replies
        threadRepository.updateReplyToUsername(username, null);

        // TODO Tournaments
        tournamentMongoRepository.deleteByAdministrator(username);
        logger.info("Redundancies deleted successfully");

        // Delete user in MongoDB
        userMongoRepository.delete(userMongo);
        logger.info("User deleted successfully");

        return "User deleted successfully";
    }

    /**
     * Retrieves a slice of reviews by username.
     *
     * @param username the username to search for
     * @param page the page number
     * @return a slice of review user DTOs
     */
    public Slice<ReviewUserDTO> getReviews(String username, int page) {
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return reviewRepository.findByAuthorUsername(username, pageable);
    }


    /*================================ LIKES =================================*/

    /**
     * Adds a like to a game by username.
     *
     * @param gameId the game ID to add the like to
     * @param username the username to add the like to
     * @return a message indicating successful addition
     */
    public String likeGame(String gameId, String username) {
        logger.info("Adding like to game with id: " + gameId + " for user with username: " + username);

        // Check if user exists
        if(userMongoRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }
        if (userNeo4jRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }

        // Check if game exists
        if (gameMongoRepository.findById(gameId).isEmpty()) {
            logger.warn("Game not found with id: " + gameId);
            throw new NotFoundException("Game not found with id: " + gameId);
        }
        if (gameNeo4jRepository.findById(gameId).isEmpty()) {
            logger.warn("Game not found with id: " + gameId);
            throw new NotFoundException("Game not found with id: " + gameId);
        }

        // Add like to game in Neo4j
        if (!userNeo4jRepository.addLikeToGame(username, gameId)) {
            logger.warn("Game not found with id: " + gameId);
            throw new NotFoundException("Game not found with id: " + gameId);
        }

        logger.info("Like added successfully");
        return "Like added successfully";

    }

    /**
     * Removes a like from a game by username.
     *
     * @param username the username to remove the like from
     * @param gameId the game ID to remove the like from
     * @return a message indicating successful removal
     */
    public String unlikeGame(String gameId, String username) {
        logger.info("Removing like from game with id: " + gameId + " for user with username: " + username);

        // Check if user exists
        if(userMongoRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }
        if (userNeo4jRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }

        // Check if game exists
        if (gameMongoRepository.findById(gameId).isEmpty()) {
            logger.warn("Game not found with id: " + gameId);
            throw new NotFoundException("Game not found with id: " + gameId);
        }
        if (gameNeo4jRepository.findById(gameId).isEmpty()) {
            logger.warn("Game not found with id: " + gameId);
            throw new NotFoundException("Game not found with id: " + gameId);
        }

        // Remove like to game in Neo4j
        if (!userNeo4jRepository.removeLikeFromGame(username, gameId)) {
            logger.warn("Game not found with id: " + gameId);
            throw new NotFoundException("Game not found with id: " + gameId);
        }

        logger.info("Like removed successfully");
        return "Like removed successfully";
    }

    /**
     * Retrieves the games liked by a user by username.
     *
     * @param username the username to search for
     * @param sortBy the sorting criteria
     * @param pageSize the number of results per page
     * @param pageNumber the page number
     * @return a list of game Neo4j objects
     */
    public List<GameLikedDTO> getLikedGames(String username, String sortBy, int pageSize, int pageNumber) {
        logger.info("Retrieving liked games for user with username: " + username);
        logger.debug("Sort by: " + sortBy + ", Page size: " + pageSize + ", Page number: " + pageNumber);

        return userNeo4jRepository.getLikedGames(username, sortBy, pageSize, pageNumber);
    }


    /*================================ FOLLOWERS =================================*/

    /**
     * Adds a follow to a user by username.
     *
     * @param username the username to add the follow to
     * @param followUsername the follow username to add
     * @return a message indicating successful addition
     */
    @Retryable(
            retryFor = {DataAccessException.class, TransactionSystemException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String followUser(String username, String followUsername) {
        logger.info("Adding follow to user with username: " + followUsername + " for user with username: " + username);

        // Check if user exists
        if(userMongoRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }
        if (userNeo4jRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }
        if(userMongoRepository.findByUsername(followUsername).isEmpty()) {
            logger.warn("User not found with username: " + followUsername);
            throw new NotFoundException("User not found with username: " + followUsername);
        }
        if (userNeo4jRepository.findByUsername(followUsername).isEmpty()) {
            logger.warn("User not found with username: " + followUsername);
            throw new NotFoundException("User not found with username: " + followUsername);
        }

        // Add follow to user in Neo4j
        if(!userNeo4jRepository.followUser(username, followUsername)){
            logger.warn("User not found");
            throw new NotFoundException("User not found");
        }
        logger.info("Follow added successfully");

        // Increment the number of followers
        userMongoRepository.incrementFollowers(followUsername);
        logger.info("Followers incremented successfully");

        return "Follow added successfully";
    }

    /**
     * Removes a follow from a user by username.
     *
     * @param username the username to remove the follow from
     * @param followUsername the follow username to remove
     * @return a message indicating successful removal
     */
    @Retryable(
            retryFor = {DataAccessException.class, TransactionSystemException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String unfollowUser(String username, String followUsername) {
        logger.info("Removing follow from user with username: " + followUsername + " for user with username: " + username);

        // Check if user exists
        if (userMongoRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }
        if (userNeo4jRepository.findByUsername(username).isEmpty()) {
            logger.warn("User not found with username: " + username);
            throw new NotFoundException("User not found with username: " + username);
        }
        if (userMongoRepository.findByUsername(followUsername).isEmpty()) {
            logger.warn("User not found with username: " + followUsername);
            throw new NotFoundException("User not found with username: " + followUsername);
        }
        if (userNeo4jRepository.findByUsername(followUsername).isEmpty()) {
            logger.warn("User not found with username: " + followUsername);
            throw new NotFoundException("User not found with username: " + followUsername);
        }

        // Remove follow from user in Neo4j
        if(!userNeo4jRepository.unfollowUser(username, followUsername)){
            logger.warn("Follow not found");
            throw new NotFoundException("Follow not found");
        }
        logger.info("Follow removed successfully");

        // Decrement the number of followers
        userMongoRepository.decrementFollowers(followUsername);
        logger.info("Followers decremented successfully");

        return "Follow removed successfully";
    }

    /**
     * Retrieves the following of a user by username.
     *
     * @param username the username to search for
     * @param sortBy the sorting criteria
     * @param pageSize the number of results per page
     * @param pageNumber the page number
     * @return a list of user Neo4j objects
     */
    public List<UserFollowsDTO> getFollowing(String username, String sortBy, int pageSize, int pageNumber) {
        logger.info("Retrieving following for user with username: " + username);
        logger.debug("Sort by: " + sortBy + ", Page size: " + pageSize + ", Page number: " + pageNumber);

        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getFollowing(username, sortBy, pageSize, pageNumber);
    }

    /**
     * Retrieves the followers of a user by username.
     *
     * @param username the username to search for
     * @param sortBy the sorting criteria
     * @param pageSize the number of results per page
     * @param pageNumber the page number
     * @return a list of user Neo4j objects
     */
    public List<UserFollowsDTO> getFollowers(String username, String sortBy, int pageSize, int pageNumber) {
        logger.info("Retrieving followers for user with username: " + username);
        logger.debug("Sort by: " + sortBy + ", Page size: " + pageSize + ", Page number: " + pageNumber);

        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getFollowers(username, sortBy, pageSize, pageNumber);
    }


    /*================================ TOURNAMENTS =================================*/

    /**
     * Retrieves the tournaments organized by a user by username.
     *
     * @param username the username to search for
     * @param currentUsername the username of the current user
     * @param sortBy the order in which the tournaments should be returned
     * @param pageSize the number of results per page
     * @param pageNumber the page number
     * @return a list of tournament Mongo objects
     */
    public List<TournamentNeo4jDTO> getOrganizedTournaments(String username, String currentUsername, String sortBy, int pageSize, int pageNumber) {
        logger.info("Retrieving organized tournaments for user with username: " + username);
        logger.debug("Sort by: " + sortBy + ", Page size: " + pageSize + ", Page number: " + pageNumber);

        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getCreatedTournaments(username, currentUsername, sortBy, pageSize, pageNumber);
    }

    /**
     * Retrieves the tournaments participated in by a user by username.
     *
     * @param username the username to search for
     * @param currentUsername the username of the current user
     * @param sortBy the order in which the tournaments should be returned
     * @param pageSize the number of results per page
     * @param pageNumber the page number
     * @return a list of tournament Mongo objects
     */
    public List<TournamentNeo4jDTO> getParticipatedTournaments(String username, String currentUsername, String sortBy, int pageSize, int pageNumber) {
        logger.info("Retrieving participated tournaments for user with username: " + username);
        logger.debug("Sort by: " + sortBy + ", Page size: " + pageSize + ", Page number: " + pageNumber);

        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getParticipatedTournaments(username, currentUsername, sortBy, pageSize, pageNumber);
    }

    /**
     * Retrieves the tournaments won by a user by username.
     *
     * @param username the username to search for
     * @param currentUsername the username of the current user
     * @param sortBy the order in which the tournaments should be returned
     * @param pageSize the number of results per page
     * @param pageNumber the page number
     * @return a list of tournament Mongo objects
     */
    public List<TournamentNeo4jDTO> getWonTournaments(String username, String currentUsername, String sortBy, int pageSize, int pageNumber) {
        logger.info("Retrieving won tournaments for user with username: " + username);
        logger.debug("Sort by: " + sortBy + ", Page size: " + pageSize + ", Page number: " + pageNumber);

        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getWonTournaments(username, currentUsername, sortBy, pageSize, pageNumber);
    }


    /*================================ ACTIVITY =================================*/

    /**
     * Retrieves the activity of the users followed by a user by username.
     *
     * @param username the username to search for
     * @param pageSize the number of results per page
     * @param pageNumber the page number
     * @return a list of followers activity DTOs
     */
    public List<FollowersActivityDTO> getFollowedActivity(String username, int pageSize, int pageNumber) {
        logger.info("Retrieving followers activity for user with username: " + username);
        logger.debug("Page size: " + pageSize + ", Page number: " + pageNumber);

        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        return userNeo4jRepository.getFollowedActivity(username, pageSize, pageNumber);
    }

    public List<PersonalActivityDTO> getPersonalActivity(String username, int pageSize, int pageNumber) {
        logger.info("Retrieving personal activity for user with username: " + username);
        logger.debug("Page size: " + pageSize + ", Page number: " + pageNumber);

        userNeo4jRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        List<PersonalActivityDTO> rawResults = userNeo4jRepository.getPersonalActivity(username, pageSize, pageNumber);
        logger.info("Raw results: {}", rawResults);
        return rawResults.stream()
                .map(dto -> new PersonalActivityDTO(
                        dto.activityType(),
                        dto.activityTime(),
                        mapActivityProperties(dto.activityProperties())
                ))
                .toList();
    }

    private ActivityPropertiesDTO mapActivityProperties(ActivityPropertiesDTO properties) {
        if (properties instanceof ActivityUserDTO userProps) {
            return userProps;
        } else if (properties instanceof ActivityTournamentDTO tournamentProps) {
            return tournamentProps;
        } else if (properties instanceof ActivityLikeDTO genericProps) {
            return genericProps;
        } else {
            throw new IllegalArgumentException("Unsupported type for ActivityPropertiesDTO: " + properties.getClass().getName());
        }
    }
}
