package com.example.BoardVerse.DTO.Game;

import com.example.BoardVerse.model.MongoDB.Location;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.Id;

import java.util.Date;

public record ReviewInfoDTO(
        //String gameId,
        String authorUsername,
        double rating,
        String comment,
        Date createdAt
) {
}
