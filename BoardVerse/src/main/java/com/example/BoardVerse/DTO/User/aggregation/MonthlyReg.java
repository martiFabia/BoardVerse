package com.example.BoardVerse.DTO.User.aggregation;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class MonthlyReg {
    @Field("_id")
    private Integer month; // Il mese (1-12)
    private Long registrations;
}
