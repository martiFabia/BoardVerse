package com.example.BoardVerse.model.MongoDB;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

@Document(collection = "users")
@Data
public class User implements UserDetails {
    @Id
    private String id;

    @NotBlank(message = "Username cannot be blank")
    @Indexed(unique = true)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;

    private Date createdAt;
    private String firstName;
    private String lastName;

    @NotBlank(message = "State or province cannot be blank")
    private String stateOrProvince;
    private String country;
    private String continent;

    private List<String> mostRecentReviews;

    private List<String> roles;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    //metodi necessari per l'integrazione con Spring Security
    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // l'account non scade
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // l'account non è bloccato
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // le credenziali non sono scadute
    }

    @Override
    public boolean isEnabled() {
        return true; // l'account è abilitato
    }


}