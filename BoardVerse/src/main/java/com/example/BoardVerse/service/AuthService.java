package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.JwtResponse;
import com.example.BoardVerse.DTO.LoginRequest;
import com.example.BoardVerse.DTO.UserRegDTO;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.repository.UserRepository;
import com.example.BoardVerse.security.jwt.JwtUtils;
import com.example.BoardVerse.security.services.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userMongoRepository;
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    public AuthService(AuthenticationManager authManager, JwtUtils jwtUtils, UserRepository userMongoRepository) {
        this.authenticationManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userMongoRepository = userMongoRepository;
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
        String role = userMongoRepository.findByUsername(userDetails.getUsername())
                .map(User::getRole)
                .orElse("ROLE_USER"); // Valore predefinito in caso di errore (non dovrebbe accadere)

        // Restituisce il DTO JwtResponse
        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role
        );
    }

    //SIGNUP
    public void registerUser(UserRegDTO signUpRequest) {
        // Verifica se il nome utente o l'email sono già registrati
        if (userMongoRepository.existsByUsername(signUpRequest.username())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userMongoRepository.existsByEmail(signUpRequest.email())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        String userId = UUID.randomUUID().toString();
    /*
        UserNeo4j newUserNeo4j = new UserNeo4j();
        newUserNeo4j.setId(userId);
        newUserNeo4j.setUsername(signUpRequest.username());
        userNeo4jRepository.save(newUserNeo4j);
        */

        // Crea un nuovo utente con ruolo fisso
        User newUserMongo = new User();
        newUserMongo.setId(userId);
        newUserMongo.setUsername(signUpRequest.username());
        newUserMongo.setPassword(encoder.encode(signUpRequest.password()));
        newUserMongo.setEmail(signUpRequest.email());
        newUserMongo.setFirstName(signUpRequest.firstName());
        newUserMongo.setLastName(signUpRequest.lastName());
        newUserMongo.setCity(signUpRequest.city());
        newUserMongo.setCountry(signUpRequest.country());
        newUserMongo.setState(signUpRequest.state());
        newUserMongo.setBirthDate(signUpRequest.birthDate());
        newUserMongo.setRole("ROLE_USER");
        newUserMongo.setCreatedAt(new Date());

        // Salva l'utente nel database
        userMongoRepository.save(newUserMongo);
    }
}
