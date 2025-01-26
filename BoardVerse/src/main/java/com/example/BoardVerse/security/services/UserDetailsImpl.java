package com.example.BoardVerse.security.services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.example.BoardVerse.model.MongoDB.UserMongo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private String id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;
    private GrantedAuthority authority;

    private final UserMongo userMongo;

    public UserDetailsImpl(UserMongo userMongo) {
        this.userMongo = userMongo; // Inizializza il campo UserMongo
        this.id = userMongo.getId();
        this.username = userMongo.getUsername();
        this.email = userMongo.getEmail();
        this.password = userMongo.getPassword();
        this.authority = new SimpleGrantedAuthority(userMongo.getRole().name());
    }

    public static UserDetailsImpl build(UserMongo userMongo) {
        return new UserDetailsImpl(userMongo);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Restituisce un singleton collection con un unico ruolo
        return List.of(authority);
    }

    public UserMongo getUser() {
        return userMongo;
    }
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
