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
import jakarta.websocket.server.PathParam;
import org.springframework.data.domain.Slice;
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
    @GetMapping("/myProfile")
    public ResponseEntity<UserInfoDTO> getMyInfo() {
        //id utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfoDTO userInfoDto = userService.getInfo(user.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }

    @Operation(summary = "Update user profile")
    @PatchMapping("/myProfile")
    public ResponseEntity<UserInfoDTO> updateUser(@RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        //utente loggato
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Recupera i dettagli dell'utente autenticato
        UserInfoDTO updatedUser= userService.updateUser(user.getId(), userUpdateDTO);

        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete user profile")
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

    @Operation(summary = "Get games liked by a user")
    @GetMapping("/{username}/likedGames")
    public ResponseEntity<?> getLikedGames(
            @PathVariable String username,
            @RequestParam(defaultValue = "alphabetical") String sortBy,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber
    ) {
        return ResponseEntity.ok(userService.getLikedGames(username, sortBy, pageSize, pageNumber));
    }


    /*================================ ACTIVITY =================================*/

    // TODO
//    @Operation(summary = "Get user activity")
//    @GetMapping("myProfile/feed")
//
//    public ResponseEntity<?> getActivity(
//            @RequestParam(defaultValue = "10") int pageSize,
//            @RequestParam(defaultValue = "1") int pageNumber
//    ) {
//        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        return ResponseEntity.ok(userService.getFollowedActivity(user.getUsername(), pageSize, pageNumber));
//    }

    // TODO
//    @Operation(summary = "Get user activity")
//    @GetMapping("/myProfile/recentActivity")
//
//    public ResponseEntity<?> getMyActivity(
//            @RequestParam(defaultValue = "10") int pageSize,
//            @RequestParam(defaultValue = "1") int pageNumber
//    ) {
//        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        return ResponseEntity.ok(userService.getPersonalActivity(user.getUsername(), pageSize, pageNumber));
//    }


    /*================================ FOLLOWERS =================================*/

    @Operation(summary = "Start following a user")
    @PostMapping("/{username}/follow")
    public ResponseEntity<String> followUser(@PathVariable String username) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.followUser(user.getUsername(), username));
    }

    @Operation(summary = "Stop following a user")
    @DeleteMapping("/{username}/follow")
    public ResponseEntity<String> unfollowUser(@PathVariable String username) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.unfollowUser(user.getUsername(), username));
    }

    @Operation(summary = "Get users followed by a user")
    @GetMapping("/{username}/following")
    public ResponseEntity<?> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "alphabetical") String sortBy,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber
    ) {
        return ResponseEntity.ok(userService.getFollowing(username, sortBy, pageSize, pageNumber));
    }

    @Operation(summary = "Get followers of a user")
    @GetMapping("/{username}/followers")
    public ResponseEntity<?> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "alphabetical") String sortBy,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber
    ) {
        return ResponseEntity.ok(userService.getFollowers(username, sortBy, pageSize, pageNumber));
    }


    /*================================ TOURNAMENTS =================================*/

    @Operation(summary = "Get list of organized tournaments by a user")
    @GetMapping("/{username}/organizedTournaments")
    public ResponseEntity<?> getOrganizedTournaments(
            @PathVariable String username,
            @RequestParam(defaultValue = "desc") String sortBy,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber
    ) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getOrganizedTournaments(username, user.getUsername(), sortBy, pageSize, pageNumber));
    }

    @Operation(summary = "Get list of participated tournaments by a user")
    @GetMapping("/{username}/participatedTournaments")
    public ResponseEntity<?> getParticipatedTournaments(
            @PathVariable String username,
            @RequestParam(defaultValue = "desc") String sortBy,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber
    ) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getParticipatedTournaments(username, user.getUsername(), sortBy, pageSize, pageNumber));
    }

    @Operation(summary = "Get list of won tournaments by a user")
    @GetMapping("/{username}/wonTournaments")
    public ResponseEntity<?> getWonTournaments(
            @PathVariable String username,
            @RequestParam(defaultValue = "desc") String sortBy,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber
    ) {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getWonTournaments(username, user.getUsername(), sortBy, pageSize, pageNumber));
    }
}
