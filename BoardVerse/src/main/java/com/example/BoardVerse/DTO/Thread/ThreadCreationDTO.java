package com.example.BoardVerse.DTO.Thread;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ThreadCreationDTO {

    @NotBlank(message = "Subject content cannot be empty")
    private String subjectContent;
    @NotBlank(message = "Tag cannot be empty")
    private String tag;
    @NotBlank(message = "Message content cannot be empty")
    private String messageContent;

}
