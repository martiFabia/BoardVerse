package com.example.BoardVerse.DTO.User.aggregation;

import lombok.Data;

import java.util.List;

@Data
public class StateAggregation {
    private String state;
    //private List<CityAggregation> cities;
    private Long count;
}
