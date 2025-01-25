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
import java.time.ZoneOffset;
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
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        // Inizio e fine dell'anno specificato
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year + 1, 1, 1);

        // Passa i valori al repository
        return userMongoRepository.monthlyRegistrations(
                startDate.atStartOfDay().toInstant(ZoneOffset.UTC), // Converti in Instant
                endDate.atStartOfDay().toInstant(ZoneOffset.UTC)    // Converti in Instant
        );
    }

}