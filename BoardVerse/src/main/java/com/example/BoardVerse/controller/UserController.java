package com.example.BoardVerse.controller;

import com.example.BoardVerse.DTO.Review.ReviewUserDTO;
import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.UserInfoDTO;
import com.example.BoardVerse.DTO.User.UserUpdateDTO;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.security.services.UserDetailsImpl;
import com.example.BoardVerse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@RequestMapping("/api/users")
@Tag(name = "UserMongo", description = "Operations related to user management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService, UserMongoRepository userMongoRepository) {
        this.userService = userService;
    }

    /* ================================ USERS CRUD ================================ */

    @Operation(summary = "Browse users")
    @GetMapping("/browse")
    public ResponseEntity<Slice<UserDTO>> getUserByUsername( @RequestParam(defaultValue = "") String username,
                                                             @RequestParam(defaultValue = "0") int page) {
        Slice<UserDTO> userDto = userService.getUserByUsername(username, page);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Get user info")
    @GetMapping("/{username}")
    public ResponseEntity<UserInfoDTO> getUserInfoByUsername(@PathVariable String username) {
        UserInfoDTO userInfoDto = userService.getInfo(username);
        return ResponseEntity.ok(userInfoDto);
    }

    @Operation(summary = "Get user profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/myProfile")
    public ResponseEntity<UserInfoDTO> getMyInfo() {
        //id utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfoDTO userInfoDto = userService.getInfo(user.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }

    @Operation(summary = "Update user profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/myProfile")
    public ResponseEntity<UserInfoDTO> updateUser(@RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        //utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Recupera i dettagli dell'utente autenticato
        UserInfoDTO updatedUser= userService.updateUser(user.getId(), userUpdateDTO);

        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete user profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/myProfile")
    public ResponseEntity<String> deleteUser() {
        //cerco username utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.deleteUser(user.getUsername()));
    }

    @Operation(summary = "Get user reviews")
    @GetMapping("/myProfile/reviews")
    public ResponseEntity<?> getMyReviews(@RequestParam(defaultValue = "0") int page) {
        //utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Slice<ReviewUserDTO> reviews = userService.getReviews(user.getUsername(), page);
        return ResponseEntity.ok(reviews);
    }

}
