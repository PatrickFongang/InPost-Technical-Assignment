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
        String url = "https://api-global-points.easypack24.net/v1/points?city=" + city;

        try {
            InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);

            if (response != null && response.getItems() != null) {
                return response.getItems();
            }
            log.warn("InPost API returned empty response for city: {}", city);
            return Collections.emptyList();
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


        return allLockers.stream()
                .filter(searchFilter)
                .sorted(Comparator.comparing(Locker::getDistance))
                .toList();
    }

}
