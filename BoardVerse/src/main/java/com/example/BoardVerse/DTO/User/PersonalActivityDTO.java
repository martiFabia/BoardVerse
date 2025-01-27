package com.example.BoardVerse.DTO.User;

import com.example.BoardVerse.DTO.User.activity.ActivityPropertiesDTO;

import java.time.OffsetDateTime;

public record PersonalActivityDTO(
        String activityType,
        OffsetDateTime activityTime,
        ActivityPropertiesDTO activityProperties
) {}
