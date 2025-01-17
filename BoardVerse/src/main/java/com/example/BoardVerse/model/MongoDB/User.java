package com.example.BoardVerse.model.MongoDB;

import com.example.BoardVerse.DTO.Review.ReviewInfoDTO;
import com.example.BoardVerse.DTO.Review.ReviewUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank
    @Min(value = 3, message = "Password must be at least 3 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "User Location (Country, State, City)")
    private Location location;

    private List<ReviewUser> mostRecentReviews=new ArrayList<>();

    @Past(message = "Birthdate must be in the past")
    private Date birthDate;

    private Date createdAt;

    private String role;     // Ruolo come stringa, es: "ROLE_USER" o "ROLE_ADMIN"

    public User get() {
        return this;
    }
/*
    public User(String username, String email, String password, String role, String city, String country, String state, Date createdAt, List<String> mostRecentReviews) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.city = city;
        this.country = country;
        this.state = state;
        this.createdAt = createdAt;
        this.mostRecentReviews = mostRecentReviews;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date date) {
        this.createdAt = date;
    }

    public List<String> getMostRecentReviews() {
        return mostRecentReviews;
    }

    public void setMostRecentReviews(List<String> mostRecentReviews) {
        this.mostRecentReviews = mostRecentReviews;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



 */
}
