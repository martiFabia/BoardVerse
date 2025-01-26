package com.example.BoardVerse.DTO;

import com.example.BoardVerse.model.MongoDB.subentities.Role;
import lombok.Data;

@Data
public class JwtResponse {
    private String id;
    private String username;
    private String email;
    private Role role;
    private String token;
    private String type = "Bearer";

    public JwtResponse(String id, String username, String email, Role role, String accessToken) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = accessToken;
    }

}