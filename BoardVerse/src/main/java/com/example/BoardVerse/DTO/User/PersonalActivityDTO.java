package com.example.BoardVerse.DTO.User;

import java.util.Date;
import java.util.Map;

public record PersonalActivityDTO(
        String activityType,
        Date timestamp,
        Map<String, Object> objectProperties
) {}
