package com.example.BoardVerse.DTO.User;

import java.time.OffsetDateTime;

public record UserFollowsDTO (
    String username,
    OffsetDateTime since
) {}