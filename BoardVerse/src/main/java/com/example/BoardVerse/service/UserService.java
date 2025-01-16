package com.example.BoardVerse.service;


import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.UserInfoDTO;
import com.example.BoardVerse.DTO.User.UserUpdateDTO;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.Location;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.repository.UserRepository;
import com.example.BoardVerse.security.jwt.JwtUtils;
import com.example.BoardVerse.utils.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        if(updates.location() != null) {
            if (userMongo.getLocation() == null) {
                // Se `userMongo` non ha una location, creiamo un nuovo oggetto
                userMongo.setLocation(new Location());
            }

            // Aggiorniamo i singoli campi della location
            if (updates.location().getCountry() != null) {
                userMongo.getLocation().setCountry(updates.location().getCountry());
            }
            if (updates.location().getState() != null) {
                userMongo.getLocation().setState(updates.location().getState());
            }
            if (updates.location().getCity() != null) {
                userMongo.getLocation().setCity(updates.location().getCity());
            }
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
