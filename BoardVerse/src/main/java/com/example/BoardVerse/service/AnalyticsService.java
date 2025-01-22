package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.User.aggregation.CountryAggregation;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.utils.Constants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final UserMongoRepository userMongoRepository;

    public AnalyticsService(UserMongoRepository userMongoRepository) {
        this.userMongoRepository = userMongoRepository;
    }

    public Slice<CountryAggregation> usersByLocation(int page) {
        Pageable pageable = PageRequest.of(page, Constants.PAGE_SIZE);
        return userMongoRepository.countUsersByLocationHierarchy(pageable);
    }
}