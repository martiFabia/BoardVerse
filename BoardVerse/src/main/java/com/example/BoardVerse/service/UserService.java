package com.example.BoardVerse.service;


import com.example.BoardVerse.DTO.*;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.repository.UserRepository;
import com.example.BoardVerse.security.jwt.JwtUtils;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.utils.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userMongoRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private AuthService authService;

    @Autowired
    public UserService(AuthenticationManager authManager, JwtUtils jwtUtils, UserRepository userMongoRepository) {
        this.authenticationManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userMongoRepository = userMongoRepository;
    }

    public UserDTO getUserByUsername(String username) {
        // Trova l'utente nel database
        Optional<User> user = userMongoRepository.findByUsername(username);
        // Se l'utente non esiste, lancia un'eccezione
        if (user.isEmpty()) {
            throw new NotFoundException("User not found with username: " + username);
        }
        // Converte l'entità User in un DTO
        return UserMapper.toDTO(user.get());
    }

    public UserInfoDTO getInfo(String username) {
        // Trova l'utente nel database
        Optional<User> user = userMongoRepository.findByUsername(username);
        // Se l'utente non esiste, lancia un'eccezione
        if (user.isEmpty()) {
            throw new NotFoundException("User not found with username: " + username);
        }
        // Converte l'entità User in un DTO
        return UserMapper.toInfoDTO(user.get());
    }


    public UserInfoDTO updateUser(String userId, UserUpdateDTO updates) {
        User userMongo = userMongoRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (updates.username() != null) {
            if (userMongoRepository.existsByUsername(updates.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
            userMongo.setUsername(updates.username());
            //userNeo4j.setUsername(userMongo.getUsername());

        }
        if (updates.password() != null) {
            userMongo.setPassword(encoder.encode(updates.password()));
        }
        if (updates.email() != null) {
            if (userMongoRepository.existsByEmail(updates.email())) {
                throw new IllegalArgumentException("Email already exists");
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

        if(updates.city() != null) {
            userMongo.setCity(updates.city());
        }

        if (updates.country() != null) {
            userMongo.setCountry(updates.country());
        }
        if (updates.state() != null) {
            userMongo.setState(updates.state());
        }

        //userNeo4jRepository.save(userNeo4j);
        userMongoRepository.save(userMongo);

        return UserMapper.toInfoDTO(userMongo.get());
    }


    public void deleteUser(String username) {
        // Trova l'utente esistente
        User user = userMongoRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        // Cancella l'utente
        userMongoRepository.delete(user);

        //eliminare utente dal graph
        //eliminare utente dai followers
        //eliminare recensioni

    }
}
