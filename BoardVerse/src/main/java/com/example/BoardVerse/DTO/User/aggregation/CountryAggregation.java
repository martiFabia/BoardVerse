package com.example.BoardVerse.DTO.User.aggregation;

import lombok.Data;

import java.util.List;

@Data
public class CountryAggregation {
    private String country;
    private List<StateAggregation> states;

}