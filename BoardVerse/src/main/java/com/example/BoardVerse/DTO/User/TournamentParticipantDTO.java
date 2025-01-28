package com.example.BoardVerse.DTO.User;

import java.time.OffsetDateTime;
import java.util.Date;

public record TournamentParticipantDTO(
        String username,
        OffsetDateTime registrationTime
) {
}
