package com.example.BoardVerse.DTO.Thread;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageCreationDTO {
    @NotBlank
    private String messageContent; // Contenuto del messaggio
}

