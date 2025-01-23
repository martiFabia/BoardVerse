package com.example.BoardVerse.service;

import com.example.BoardVerse.DTO.User.aggregation.CountryAggregation;
import com.example.BoardVerse.DTO.User.aggregation.MonthlyReg;
import com.example.BoardVerse.repository.UserMongoRepository;
import com.example.BoardVerse.utils.Constants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

    public List<MonthlyReg> monthlyRegistrations(Integer year) {
        if(year == null){
            //current year
            year = LocalDate.now().getYear();
        }

        return userMongoRepository.monthlyRegistrations(year);
    }
}