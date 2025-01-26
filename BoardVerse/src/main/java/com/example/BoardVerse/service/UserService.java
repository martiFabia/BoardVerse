package com.example.BoardVerse.service;


import com.example.BoardVerse.DTO.Review.ReviewUserDTO;
import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.UserInfoDTO;
import com.example.BoardVerse.DTO.User.UserUpdateDTO;
import com.example.BoardVerse.exception.AlreadyExistsException;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.subentities.Location;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.repository.ReviewRepository;
import com.example.BoardVerse.repository.ThreadRepository;
import com.example.BoardVerse.repository.TournamentMongoRepository;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.security.jwt.JwtUtils;
import com.example.BoardVerse.utils.Constants;
import com.example.BoardVerse.utils.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserMongoRepository userMongoRepository;
    private ReviewRepository reviewRepository;
    private ThreadRepository threadRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private AuthService authService;
    @Autowired
    private TournamentMongoRepository tournamentMongoRepository;

    @Autowired
    public UserService(AuthenticationManager authManager, JwtUtils jwtUtils, UserMongoRepository userMongoRepository, ReviewRepository reviewRepository, ThreadRepository threadRepository, TournamentMongoRepository tournamentMongoRepository) {
        this.authenticationManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userMongoRepository = userMongoRepository;
        this.reviewRepository = reviewRepository;
        this.threadRepository = threadRepository;
        this.tournamentMongoRepository = tournamentMongoRepository;
    }

    public Slice<UserDTO> getUserByUsername(String username, int page) {
        // Trova l'utente nel database
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return userMongoRepository.findByUsernameContaining(username, pageable);
    }

    public UserInfoDTO getInfo(String username) {
        // Trova l'utente nel database
        Optional<UserMongo> user = userMongoRepository.findByUsername(username);
        // Se l'utente non esiste, lancia un'eccezione
        if (user.isEmpty()) {
            throw new NotFoundException("UserMongo not found with username: " + username);
        }
        // Converte l'entità UserMongo in un DTO
        return UserMapper.toInfoDTO(user.get());
    }


    @Transactional
    public UserInfoDTO updateUser(String userId, UserUpdateDTO updates) {
        UserMongo userMongo = userMongoRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("UserMongo not found with id: " + userId));

        if (updates.username() != null) {
            if (userMongoRepository.existsByUsername(updates.username())) {
                throw new AlreadyExistsException("Username already exists");
            }
            //aggiorno reviews
            reviewRepository.updateUsernameInReviews(userMongo.getUsername(), updates.username());

            //Si aggiorna l'username dell'utente nei thread e nei messaggi e nelle risposte
            threadRepository.updateThreadAuthorUsername(userMongo.getUsername(), updates.username());
            threadRepository.updateMessageAuthorUsername(userMongo.getUsername(), updates.username());
            threadRepository.updateReplyToUsername(userMongo.getUsername(), updates.username());

            //aggiorno tornei
            tournamentMongoRepository.updateAdministratorInTournaments(userMongo.getUsername(), updates.username());
            tournamentMongoRepository.updateWinnerInTournaments(userMongo.getUsername(), updates.username());

            userMongo.setUsername(updates.username());
            //userNeo4j.setUsername(userMongo.getUsername());

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
                // Se `userMongo` non ha una location, creiamo un nuovo oggetto
                userMongo.setLocation(new Location());
            }
            // Aggiorniamo i singoli campi della location
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

        //userNeo4jRepository.save(userNeo4j);
        userMongoRepository.save(userMongo);

        return UserMapper.toInfoDTO(userMongo.get());

        //TODO AGGIORNARE ANCHE SU GRAPH
    }


    public String deleteUser(String username) {
        // Trova l'utente esistente
        UserMongo userMongo = userMongoRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("UserMongo not found with username: " + username));
        // Cancella l'utente
        userMongoRepository.delete(userMongo);

        //nelle recensioni imposto autore a null
        reviewRepository.updateUsernameInReviews(username, null);


        //Si aggiorna l'username dell'utente nei thread e nei messaggi e nelle risposte
        //impostandolo a null (l'utente è stato eliminato)
        threadRepository.updateThreadAuthorUsername(username, null);
        threadRepository.updateMessageAuthorUsername(username, null);
        threadRepository.updateReplyToUsername(username, null);

        //elimino tornei creati dall'utente
        tournamentMongoRepository.deleteByAdministrator(username);



        //ELIMINARE UTENTE DAL GRAPH (E TUTTE LE RELAZIONI)

        return "UserMongo deleted successfully";


    }

    public Slice<ReviewUserDTO> getReviews(String username, int page) {
        // Trova le recensioni dell'utente
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return reviewRepository.findByAuthorUsername(username, pageable);
    }
}
