package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.JwtResponse;
import com.example.BoardVerse.DTO.User.LoginRequest;
import com.example.BoardVerse.DTO.User.UserRegDTO;
import com.example.BoardVerse.exception.AlreadyExistsException;
import com.example.BoardVerse.model.MongoDB.UserMongo;
import com.example.BoardVerse.model.MongoDB.subentities.Role;
import com.example.BoardVerse.model.MongoDB.subentities.TournamentsUser;
import com.example.BoardVerse.model.Neo4j.UserNeo4j;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.repository.UserNeo4jRepository;
import com.example.BoardVerse.security.jwt.JwtUtils;
import com.example.BoardVerse.security.services.UserDetailsImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.util.Date;
import java.util.UUID;


@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMongoRepository userMongoRepository;
    private final UserNeo4jRepository userNeo4jRepository;

    @Autowired
    PasswordEncoder encoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    public AuthService(
            AuthenticationManager authManager,
            JwtUtils jwtUtils,
            UserMongoRepository userMongoRepository,
            UserNeo4jRepository userNeo4jRepository
    ) {
        this.authenticationManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userMongoRepository = userMongoRepository;
        this.userNeo4jRepository = userNeo4jRepository;
    }

    //LOGIN
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Autenticazione dell'utente
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Configura il contesto di sicurezza con i dettagli dell'utente autenticato
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Genera il token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);
        // Recupera i dettagli dell'utente autenticato
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Recupera il ruolo dal database
        Role role = userMongoRepository.findByUsername(userDetails.getUsername())
                .map(UserMongo::getRole)
                .orElse(Role.ROLE_USER); // Valore predefinito in caso di errore (non dovrebbe accadere)

        // Restituisce il DTO JwtResponse
        return new JwtResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role,
                jwt
        );
    }

    //SIGNUP
    @Retryable(
            retryFor = {DataAccessException.class, TransactionSystemException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String registerUser(UserRegDTO signUpRequest) {
        logger.info("Registering new user: " + signUpRequest.username());

        // Verifica se il nome utente o l'email sono già registrati
        if (userMongoRepository.existsByUsername(signUpRequest.username())) {
            logger.warn("Username already taken: " + signUpRequest.username());
            throw new AlreadyExistsException("Error: Username is already taken!");
        }
        if (userMongoRepository.existsByEmail(signUpRequest.email())) {
            logger.warn("Email already in use: " + signUpRequest.email());
            throw new AlreadyExistsException("Error: Email is already in use!");
        }
        if (userNeo4jRepository.findByUsername(signUpRequest.username()).isPresent()) {
            logger.warn("Username already taken: " + signUpRequest.username());
            throw new AlreadyExistsException("Error: Username is already taken!");
        }

        // Clean location
        if(
            signUpRequest.location().getCity().isEmpty() ||
            signUpRequest.location().getCity().equals(" ") ||
            signUpRequest.location().getCity().equals("null") ||
            signUpRequest.location().getCity().equals("undefined")
        )
            signUpRequest.location().setCity(null);
        if(
            signUpRequest.location().getStateOrProvince().isEmpty() ||
            signUpRequest.location().getStateOrProvince().equals(" ") ||
            signUpRequest.location().getStateOrProvince().equals("null") ||
            signUpRequest.location().getStateOrProvince().equals("undefined")
        )
            signUpRequest.location().setStateOrProvince(null);
        if(
            signUpRequest.location().getCountry().isEmpty() ||
            signUpRequest.location().getCountry().equals(" ") ||
            signUpRequest.location().getCountry().equals("null") ||
            signUpRequest.location().getCountry().equals("undefined")
        )
            signUpRequest.location().setCountry(null);
        logger.debug("Location: " + signUpRequest.location().toString());

        String userId = UUID.randomUUID().toString();

        TournamentsUser newUserTournaments = new TournamentsUser();
        newUserTournaments.setCreated(0);
        newUserTournaments.setPartecipated(0);
        newUserTournaments.setWon(0);

        // Crea un nuovo utente con ruolo fisso
        UserMongo newUserMongoMongo = new UserMongo();
        newUserMongoMongo.setId(userId);
        newUserMongoMongo.setUsername(signUpRequest.username());
        newUserMongoMongo.setPassword(encoder.encode(signUpRequest.password()));
        newUserMongoMongo.setEmail(signUpRequest.email());
        newUserMongoMongo.setFirstName(signUpRequest.firstName());
        newUserMongoMongo.setLastName(signUpRequest.lastName());
        newUserMongoMongo.setLocation(signUpRequest.location());
        newUserMongoMongo.setBirthDate(signUpRequest.birthDate());
        newUserMongoMongo.setFollowers(0);
        newUserMongoMongo.setFollowing(0);
        newUserMongoMongo.setTournaments(newUserTournaments); // Non ci sono tornei associati
        newUserMongoMongo.setRole(Role.ROLE_USER);
        newUserMongoMongo.setRegisteredDate(new Date());

        // Neo4j
        UserNeo4j UserNeo4j = new UserNeo4j();
        UserNeo4j.setUsername(signUpRequest.username());
        userNeo4jRepository.save(UserNeo4j);
        logger.info("UserNeo4j saved: " + UserNeo4j);

        // MongoDB
        userMongoRepository.save(newUserMongoMongo);

        return "User registered successfully!";
    }
}
