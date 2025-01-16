package com.example.BoardVerse.DTO.Game;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddReviewDTO {
    @Min(1) @Max(10)
    private Double rating;
    private String comment;
}
