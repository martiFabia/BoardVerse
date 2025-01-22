package com.example.BoardVerse.security.services;

/* Recupera un utente dal database tramite username utilizzando  UserRepository .
Converte l'oggetto utente in un'implementazione di UserDetails (UserDetailsImpl)
che Spring Security utilizza per verificare le credenziali e gestire l'autenticazione. */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.repository.UserMongoRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserMongoRepository userMongoRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMongoRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }


    // Nuovo metodo per caricare l'utente usando l'ID
    @Transactional
    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        User user = userMongoRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));

        return UserDetailsImpl.build(user);
    }

}