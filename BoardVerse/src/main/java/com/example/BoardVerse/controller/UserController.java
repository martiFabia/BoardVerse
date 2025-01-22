package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.UserInfoDTO;
import com.example.BoardVerse.DTO.User.UserUpdateDTO;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;
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
    private final UserMongoRepository userMongoRepository;

    public UserController(UserService userService, UserMongoRepository userMongoRepository) {
        this.userService = userService;
        this.userMongoRepository = userMongoRepository;
    }

    /* ================================ USERS CRUD ================================ */

    //restitusice solo username ed email
    @GetMapping("/browse")
    public ResponseEntity<Slice<UserDTO>> getUserByUsername( @RequestParam(defaultValue = "") String username,
                                                             @RequestParam(defaultValue = "0") int page) {
        Slice<UserDTO> userDto = userService.getUserByUsername(username, page);
        return ResponseEntity.ok(userDto);
    }

    //restituisce tutti i dati dell'utente tranne la password
    @GetMapping("/{username}")
    public ResponseEntity<UserInfoDTO> getUserInfoByUsername(@PathVariable String username) {
        UserInfoDTO userInfoDto = userService.getInfo(username);
        return ResponseEntity.ok(userInfoDto);
    }

    //restituisce pagina profilo utente loggato
    @GetMapping("/myProfile")
    public ResponseEntity<UserInfoDTO> getMyInfo() {
        //id utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfoDTO userInfoDto = userService.getInfo(user.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }

    //aggiorna i dati dell'utente
    @PatchMapping("/myProfile")
    public ResponseEntity<UserInfoDTO> updateUser(@RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        //utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Recupera i dettagli dell'utente autenticato
        UserInfoDTO updatedUser= userService.updateUser(user.getId(), userUpdateDTO);

        return ResponseEntity.ok(updatedUser);
    }

    //elimina l'utente loggato
    @DeleteMapping("/myProfile")
    public ResponseEntity<String> deleteUser() {
        //cerco username utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.deleteUser(user.getUsername());
        return ResponseEntity.ok("User deleted successfully");
    }







}
