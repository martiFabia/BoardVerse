package com.example.BoardVerse.DTO.Game;

public record GameAnalyticsDTO(
        String gameId,
        String gameName,
        int likeCount,
        int totalTournaments,
        int finishedTournaments,
        int ongoingTournaments,
        int futureTournaments,
        int publicTournaments,
        int privateTournaments,
        int inviteTournaments
) {
}
