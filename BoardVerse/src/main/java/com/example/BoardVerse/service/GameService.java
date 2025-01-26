package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.Game.*;
import com.example.BoardVerse.exception.AlreadyExistsException;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.config.GlobalExceptionHandler;
import com.example.BoardVerse.model.MongoDB.GameMongo;
import com.example.BoardVerse.repository.*;
import com.example.BoardVerse.utils.Constants;
import com.example.BoardVerse.utils.MongoGameMapper;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.BoardVerse.utils.MongoGameMapper.mapDTOToGameMongo;

@Service
public class GameService {

    private final GameMongoRepository gameMongoRepository;
    private final ReviewRepository reviewRepository;
    private final TournamentMongoRepository tournamentMongoRepository;
    private final ThreadRepository threadRepository;
    private final UserMongoRepository userMongoRepository;

    public GameService(GameMongoRepository gameMongoRepository, ReviewRepository reviewRepository, TournamentMongoRepository tournamentMongoRepository, ThreadRepository threadRepository, UserMongoRepository userMongoRepository) {
        this.gameMongoRepository = gameMongoRepository;
        this.reviewRepository = reviewRepository;
        this.tournamentMongoRepository = tournamentMongoRepository;
        this.threadRepository = threadRepository;
        this.userMongoRepository = userMongoRepository;
    }

    // Operazione di creazione
    public String addNewGame(GameCreationDTO newGameDTO) {

        if (gameMongoRepository.findByNameAndYearReleased(newGameDTO.getName(), newGameDTO.getYearReleased()).isPresent()) {
            throw new AlreadyExistsException("Game " + newGameDTO.getName() + " released in " + newGameDTO.getYearReleased() + " already exists");
        }

        GameMongo newGameMongo = mapDTOToGameMongo(newGameDTO);
        gameMongoRepository.save(newGameMongo);
        return "Game " + newGameMongo.getName() + " released in " + newGameDTO.getYearReleased() + " added successfully";

    }


    public String updateGame(String gameId, GameUpdateDTO updateGameDTO) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        System.out.println("Year: " + updateGameDTO.getYearReleased());

        //Se è presente un gioco con lo stesso nome e anno di rilascio,
        // ma questo gioco non è quello che si sta cercando di aggiornare
        // allora lancia un'eccezione
        if(updateGameDTO.getName() != null && updateGameDTO.getYearReleased() != null) {
            Optional<GameMongo> existingGame = gameMongoRepository.findByNameAndYearReleased(updateGameDTO.getName(), updateGameDTO.getYearReleased());
            if (existingGame.isPresent() && !existingGame.get().getId().equals(gameId)) {
                throw new AlreadyExistsException("Game " + updateGameDTO.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists.");
            }
        }

        if(updateGameDTO.getName() != null) {
            Optional<GameMongo> existingGame = gameMongoRepository.findByNameAndYearReleased(updateGameDTO.getName(), game.getYearReleased());
            if (existingGame.isPresent() && !existingGame.get().getId().equals(gameId)) {
                throw new AlreadyExistsException("Game " + updateGameDTO.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists.");
            }
            game.setName(updateGameDTO.getName());
            //aggiornamento nome in THREAD
            threadRepository.updateGameByGameId(gameId, updateGameDTO.getName());
            //aggiornamento nome in review
            reviewRepository.updateGameNameInReviews(gameId, updateGameDTO.getName());
            //aggiornamento nome in tournament
            tournamentMongoRepository.updateGameNameInTournaments(gameId, updateGameDTO.getName());
            //aggiornamento nome in user.mostRecentReviews
            userMongoRepository.updateGameNameInMostRecentReviews(gameId, updateGameDTO.getName());
        }

        if(updateGameDTO.getYearReleased() != null) {
            Optional<GameMongo> existingGame = gameMongoRepository.findByNameAndYearReleased(game.getName(), updateGameDTO.getYearReleased());
            if (existingGame.isPresent() && !existingGame.get().getId().equals(gameId)) {
                throw new AlreadyExistsException("Game " + game.getName() + " released in " + updateGameDTO.getYearReleased() + " already exists.");
            }
            game.setYearReleased(updateGameDTO.getYearReleased());
            //aggiornamento anno in THREAD
            threadRepository.updateYearByGameId(gameId, updateGameDTO.getYearReleased());
            //aggiornamento anno in REVIEWS
            reviewRepository.updateGameYearInReviews(gameId, updateGameDTO.getYearReleased());
            //aggiornamento anno in TOURNAMENTS
            tournamentMongoRepository.updateGameYearInTournaments(gameId, updateGameDTO.getYearReleased());
            //aggiornamento anno in user.mostRecentReviews
            userMongoRepository.updateGameYearInMostRecentReviews(gameId, updateGameDTO.getYearReleased());
        }

        if (updateGameDTO.getShortDescription()!=null) {
            game.setShortDescription(updateGameDTO.getShortDescription());
            //aggiornamento shortDescription in review
            reviewRepository.updateGameShortDescInReviews(gameId, updateGameDTO.getShortDescription());
        }

        if(updateGameDTO.getDescription() != null){
            game.setDescription(updateGameDTO.getDescription());
        }

        if(updateGameDTO.getMinPlayers()!=null) {
            game.setMinPlayers(updateGameDTO.getMinPlayers());
        }

        if(updateGameDTO.getMaxPlayers()!=null) {
            game.setMaxPlayers(updateGameDTO.getMaxPlayers());
        }

        if(updateGameDTO.getMinSuggAge()!=null) {
            game.setMinSuggAge(updateGameDTO.getMinSuggAge());
        }

        if (updateGameDTO.getMinPlayTime()!=null) {
            game.setMinPlayTime(updateGameDTO.getMinPlayTime());
        }

        if (updateGameDTO.getMaxPlayTime()!=null) {
            game.setMaxPlayTime(updateGameDTO.getMaxPlayTime());
        }

        if (updateGameDTO.getDesigners()!=null) {
            game.setDesigners(updateGameDTO.getDesigners());
        }
        if (updateGameDTO.getArtists()!=null)
            game.setArtists(updateGameDTO.getArtists());

        if (updateGameDTO.getPublisher()!=null)
            game.setPublisher(updateGameDTO.getPublisher());

        if(updateGameDTO.getCategories()!=null && !updateGameDTO.getCategories().isEmpty()) {
            game.setCategories(updateGameDTO.getCategories());
        }

        if(updateGameDTO.getMechanics()!=null)
            game.setMechanics(updateGameDTO.getMechanics());

        if(updateGameDTO.getFamily()!=null)
            game.setFamily(updateGameDTO.getFamily());

        gameMongoRepository.save(game);
        return "Game " + game.getId() + " updated successfully";

        //SE MODIFICATA SHORT DESC MODIFICARE GRAPH
    }


    // Operazione di eliminazione
    public String deleteGame(String gameId) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        gameMongoRepository.delete(game);

        threadRepository.deleteAllByGameId(gameId);
        //eliminare reviews
        reviewRepository.deleteByGameId(gameId);
        //eliminare tornei
        tournamentMongoRepository.deleteByGameId(gameId);
        //eliminare dai mostRecentReviews degli utenti
        userMongoRepository.deleteGameFromMostRecentReviews(gameId);

        return "Game with id " + gameId + " deleted successfully";

        //ELIMINARE DAL GRAPH (E TUTTE LE RELAZIONI)

    }

    // Operazione di ricerca per nome
    public Slice<GamePreviewDTO> findByName(String name, int page) {
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return gameMongoRepository.findByNameContaining(name, pageable);
    }

    public GameInfoDTO getInfo(String gameId) {
        GameMongo game = gameMongoRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        return MongoGameMapper.toDTO(game);
    }


    public RatingDetails getRatingsDetails(String gameId) {
        return reviewRepository.findRatingDetailsByGameId(gameId);
    }

    public Slice<GamePreviewDTO> getFilteredGames(Integer yearReleased, String categories,
                                                  String mechanics, String sortBy, String order, int page) {
        //Determina su quale campo ordinare
        Sort sort;
        if ("yearReleased".equalsIgnoreCase(sortBy)) {
            // Se vuoi considerare asc/desc
            sort = "asc".equalsIgnoreCase(order)
                    ? Sort.by("yearReleased").ascending()
                    : Sort.by("yearReleased").descending();
        } else {
            // di default ordiniamo su averageRating
            sort = "asc".equalsIgnoreCase(order)
                    ? Sort.by("averageRating").ascending()
                    : Sort.by("averageRating").descending();
        }

        // 10 elementi per pagina.
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE, sort);

        return gameMongoRepository.findGamesByFilters(
                yearReleased, categories, mechanics, pageable
        );
    }


    public Slice<GamePreviewDTO> getRanking(Date startDate, Date endDate, String country, String state, String city, int page) {
        if(startDate == null){
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }

        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return reviewRepository.findAverageRatingByPostDateLocation(startDate, endDate, country, state, city, pageable);
    }

    public List<MostPlayedGameDTO> getMostPlayedGames() {
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date();
        calendar.add(Calendar.YEAR, -1);
        Date startDate = calendar.getTime();

        return tournamentMongoRepository.findTop10GamesWithHighestAverageParticipation(startDate, endDate);
    }


    public List<BestGameAgeDTO> bestGamesByAge() {
        return reviewRepository.findBestGameByAgeBrackets();
    }


}