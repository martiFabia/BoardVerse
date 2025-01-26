package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.User.UserDTO;
import com.example.BoardVerse.DTO.User.UserInfoDTO;
import com.example.BoardVerse.model.MongoDB.UserMongo;

public class UserMapper {
    public static UserDTO toDTO(UserMongo userMongo) {
        return new UserDTO(userMongo.getUsername(), userMongo.getEmail());
    }

    public static UserInfoDTO toInfoDTO(UserMongo userMongo) {
        return new UserInfoDTO(userMongo.getId(), userMongo.getUsername(), userMongo.getEmail(), userMongo.getFirstName(),
                userMongo.getLastName(), userMongo.getRegisteredDate(), userMongo.getLocation(), userMongo.getBirthDate(),
                userMongo.getFollowers(), userMongo.getFollowing(), userMongo.getTournaments(), userMongo.getMostRecentReviews());
    }

}

