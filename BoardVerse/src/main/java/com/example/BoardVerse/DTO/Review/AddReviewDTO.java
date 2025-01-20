package com.example.BoardVerse.DTO.Review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddReviewDTO {
    @Min(1) @Max(10)
    private Double rating;
    @NotEmpty
    private String comment;
}
