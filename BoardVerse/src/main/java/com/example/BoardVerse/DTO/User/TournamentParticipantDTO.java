package com.example.BoardVerse.DTO.User;

import java.util.Date;

public record TournamentParticipantDTO(
        String username,
        Date registrationTime
) {
}
