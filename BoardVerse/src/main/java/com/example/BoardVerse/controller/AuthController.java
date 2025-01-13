package com.example.BoardVerse.controller;

import com.example.BoardVerse.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Simula un controllo delle credenziali
        if ("admin".equals(request.getUsername()) && "password".equals(request.getPassword())) {
            String token = jwtTokenUtil.generateToken(request.getUsername(), "ROLE_ADMIN");
            return ResponseEntity.ok(new TokenResponse(token));
        } else if ("user".equals(request.getUsername()) && "password".equals(request.getPassword())) {
            String token = jwtTokenUtil.generateToken(request.getUsername(), "ROLE_USER");
            return ResponseEntity.ok(new TokenResponse(token));
        }
        return ResponseEntity.status(401).body("Credenziali non valide");
    }
}
