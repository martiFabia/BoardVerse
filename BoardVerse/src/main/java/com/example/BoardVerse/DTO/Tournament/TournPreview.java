package com.example.BoardVerse.DTO.Tournament;

import lombok.Data;

import java.util.Date;

@Data
public class TournPreview {

    private String id;
    private String name;
    private String type;
    private Date startingTime;
    private Integer numParticipants;
    private Integer minParticipants;
    private Integer maxParticipants;
    private String administrator;
    private String winner;

}