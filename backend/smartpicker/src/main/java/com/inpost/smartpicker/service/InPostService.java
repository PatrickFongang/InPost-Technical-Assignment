package com.inpost.smartpicker.service;

import com.inpost.smartpicker.dto.search.LockerSearchRequestDto;
import com.inpost.smartpicker.dto.search.LockerSearchResponseDto;
import com.inpost.smartpicker.dto.weather.WeatherInfoDto;
import com.inpost.smartpicker.exception.WeatherApiException;
import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.model.enums.Reliability;
import com.inpost.smartpicker.util.DistanceCalculator;
import com.inpost.smartpicker.util.GeoGridUtil;
import com.inpost.smartpicker.util.ReliabilityScorer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static com.inpost.smartpicker.predicate.LockerSearchPredicates.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class InPostService {

    private final LocalLockerCache localLockerCache;
    private final DistanceCalculator distanceCalculator;
    private final WeatherService weatherService;

    public LockerSearchResponseDto searchLockers(LockerSearchRequestDto request) {
        log.info("Searching for lockers near coordinates: [{}, {}], radius={}km",
                request.userLat(), request.userLon(), request.radiusInKm());

        int searchDepth = request.radiusInKm().intValue() + 1;
        List<String> targetGrids = GeoGridUtil.getNeighboringGridKeys(request.userLat(), request.userLon(), searchDepth);

        List<Locker> areaLockers = localLockerCache.getLockersForGrids(targetGrids);

        if (areaLockers.isEmpty()) {
            log.info("No lockers found in the surrounding geospatial grids.");
            return new LockerSearchResponseDto(areaLockers, null);
        }

        areaLockers.forEach(locker -> {
            double distance = distanceCalculator.calculateDistanceInKm(
                    request.userLat(), request.userLon(),
                    locker.getLocation().getLatitude(), locker.getLocation().getLongitude()
            );
            locker.setDistance(Math.round(distance * 100.0) / 100.0);

            locker.setEasyAccessReliability(ReliabilityScorer.calculateEasyAccessScore(locker));
            locker.setStressFreeReliability(ReliabilityScorer.calculateStressFreeScore(locker));
        });

        Predicate<Locker> searchFilter = isWithinRadius(request.radiusInKm());

        if (request.thermoMode()) {
            searchFilter = searchFilter.and(isThermoFriendly());
        }

        List<Locker> filteredLockers = areaLockers.stream()
                .filter(searchFilter)
                .sorted(Comparator.comparing(Locker::getDistance))
                .toList();

        log.info("Found {} lockers matching criteria out of {} machines in the search sector.",
                filteredLockers.size(), areaLockers.size());

        return new LockerSearchResponseDto(filteredLockers, handleWeatherInfo(request));
    }

    private WeatherInfoDto handleWeatherInfo(LockerSearchRequestDto request) {
        if (request.expectedDeliveryDate() == null) return null;

        try {
            return weatherService.getWeatherForecast(request.userLat(), request.userLon(), request.expectedDeliveryDate());
        } catch (WeatherApiException e) {
            log.warn("Weather integration failed. Proceeding without weather warnings. Reason: {}", e.getMessage());
            return null;
        }
    }

}
