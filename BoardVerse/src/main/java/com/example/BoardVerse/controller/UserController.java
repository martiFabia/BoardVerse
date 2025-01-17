package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.UserInfoDTO;
import com.example.BoardVerse.DTO.User.UserUpdateDTO;
import com.example.BoardVerse.repository.UserRepository;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/getUserbyUsername")
    public ResponseEntity<UserDTO> getUserByUsername(@RequestParam String username) {
        UserDTO userDto = userService.getUserByUsername(username);
        return ResponseEntity.ok(userDto);
    }

    //restituisce tutti i dati dell'utente tranne la password
    @GetMapping("/getInfo")
    public ResponseEntity<UserInfoDTO> getUserInfoByUsername(@RequestParam String username) {
        UserInfoDTO userInfoDto = userService.getInfo(username);
        return ResponseEntity.ok(userInfoDto);
    }

    //restituisce dati dell'utente loggato
    @GetMapping("/getProfile")
    public ResponseEntity<UserInfoDTO> getMyInfo() {
        //id utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfoDTO userInfoDto = userService.getInfo(user.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }


    //aggiorna i dati dell'utente
    @PatchMapping("/updateProfile")
    public ResponseEntity<UserInfoDTO> updateUser(@RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        //utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Recupera i dettagli dell'utente autenticato
        UserInfoDTO updatedUser= userService.updateUser(user.getId(), userUpdateDTO);

        return ResponseEntity.ok(updatedUser);
    }


    //elimina l'utente
    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> deleteUser() {
        //cerco username utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.deleteUser(user.getUsername());
        return ResponseEntity.ok("User deleted successfully");
    }







}
