package com.inpost.smartpicker.service;

import com.inpost.smartpicker.dto.search.LockerSearchRequestDto;
import com.inpost.smartpicker.exception.InPostApiException;
import com.inpost.smartpicker.model.InPostResponse;
import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static com.inpost.smartpicker.predicate.LockerPredicates.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class InPostService {
    private final RestTemplate restTemplate;
    private final DistanceCalculator distanceCalculator;



    public List<Locker> fetchLockersByCity(String city) {
        List<Locker> allLockers = new ArrayList<>();

        int currentPage = 1;
        int totalPages = 1;

        try {
            do {
                String url = String.format("https://api-global-points.easypack24.net/v1/points?city=%s&page=%d", city, currentPage);
                log.debug("Fetching page {} for city {}", currentPage, city);

                InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);

                if (response != null) {
                    if (response.getItems() != null) {
                        allLockers.addAll(response.getItems());
                    }
                    if (response.getTotalPages() != null) {
                        totalPages = response.getTotalPages();
                    }
                }

                currentPage++;

            } while (currentPage <= totalPages);

            log.info("Successfully fetched {} lockers across {} pages for city: {}", allLockers.size(), totalPages, city);
            return allLockers;

        } catch (Exception e) {
            log.error("Error communicating with InPost API for city {}. Details: {}", city, e.getMessage());
            throw new InPostApiException("Failed to fetch data from InPost API for city: " + city);
        }
    }
    public List<Locker> searchLockers(LockerSearchRequestDto request) {
        log.info("Searching for lockers: city={}, radius={}, stressFree={}, thermo={}",
                request.city(), request.radiusInKm(), request.stressFreeMode(), request.thermoMode());

        List<Locker> allLockers = fetchLockersByCity(request.city());

        allLockers.forEach(locker -> {
            if (locker.getLocation() != null && locker.getLocation().getLatitude() != null && locker.getLocation().getLongitude() != null) {
                double distance = distanceCalculator.calculateDistanceInKm(
                        request.userLat(), request.userLon(),
                        locker.getLocation().getLatitude(), locker.getLocation().getLongitude()
                );
                locker.setDistance(Math.round(distance * 100.0) / 100.0);
            }
        });

        Predicate<Locker> searchFilter = isWithinRadius(request.radiusInKm());

        if (request.stressFreeMode()) {
            searchFilter = searchFilter.and(isStressFree());
        }

        if (request.thermoMode()) {
            searchFilter = searchFilter.and(isThermoFriendly());
        }

        List<Locker> filteredLockers = allLockers.stream()
                .filter(searchFilter)
                .sorted(Comparator.comparing(Locker::getDistance))
                .toList();

        log.info("Found {} lockers matching criteria in {} (out of {} total in city)",
                filteredLockers.size(), request.city(), allLockers.size());

        return filteredLockers;
    }

}
