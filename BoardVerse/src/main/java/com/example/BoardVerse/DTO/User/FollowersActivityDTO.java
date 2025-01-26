package com.example.BoardVerse.DTO.User;

import lombok.Data;

import java.util.Date;
import java.util.Map;

public record FollowersActivityDTO(
    String followerUsername,
    String activityType,
    Date timestamp,
    Map<String, Object> objectProperties
){}
