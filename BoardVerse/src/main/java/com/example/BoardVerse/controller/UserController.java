package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.*;
import com.example.BoardVerse.exception.NotFoundException;
import com.example.BoardVerse.model.MongoDB.User;
import com.example.BoardVerse.repository.UserRepository;
import com.example.BoardVerse.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Operations related to user management")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /* ================================ USERS CRUD ================================ */

    //restitusice solo username ed email
    @GetMapping("/searchUser")
    public ResponseEntity<UserDTO> getUserByUsername(@RequestParam String username) {
        UserDTO userDto = userService.getUserByUsername(username);
        return ResponseEntity.ok(userDto);
    }

    //restituisce tutti i dati dell'utente tranne la password
    @GetMapping("/UserInfo")
    public ResponseEntity<UserInfoDTO> getUserInfoByUsername(@RequestParam String username) {
        UserInfoDTO userInfoDto = userService.getInfo(username);
        return ResponseEntity.ok(userInfoDto);
    }

    /*
    //aggiorna i dati dell'utente
    @PatchMapping("/updateUser")
    public ResponseEntity<MessageResponse> updateUser(@RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        //cerco username utente loggato
        String username =  SecurityContextHolder.getContext().getAuthentication().getName();
        //stampa username
        System.out.println(username);
        MessageResponse update= userService.updateUser(username, userUpdateDTO);

        return ResponseEntity.ok(update);
    }

     */

    //elimina l'utente
    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> deleteUser() {
        //cerco username utente loggato
        String username =  SecurityContextHolder.getContext().getAuthentication().getName();
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully");
    }




}
