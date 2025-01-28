package com.example.BoardVerse.DTO.User;

import java.time.OffsetDateTime;
import java.util.Date;

public record GameLikesUserList(
        String username,
        OffsetDateTime timestamp
) {
}
