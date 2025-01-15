package com.example.BoardVerse.utils;

import com.example.BoardVerse.DTO.UserDTO;
import com.example.BoardVerse.DTO.UserInfoDTO;
import com.example.BoardVerse.model.MongoDB.User;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        return new UserDTO(user.getUsername(), user.getEmail());
    }

    public static UserInfoDTO toInfoDTO(User user) {
        return new UserInfoDTO(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getCity(), user.getCountry(), user.getState(), user.getBirthDate());
    }
}

