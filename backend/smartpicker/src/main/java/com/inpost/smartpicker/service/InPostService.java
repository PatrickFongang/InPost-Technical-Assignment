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

    /**
     * Searches for lockers based on the provided search criteria.
     * <p>
     * The search process involves:
     * 1. Fetching lockers from geospatial grids corresponding to the search area.
     * 2. Enriching the found lockers with distance and reliability metrics.
     * 3. Filtering and sorting the lockers based on user preferences.
     * 4. Optionally retrieving weather information for the expected delivery date.
     * </p>
     *
     * @param request the {@link LockerSearchRequestDto} containing coordinates, radius, and filter options
     * @return a {@link LockerSearchResponseDto} containing the list of matching lockers and weather information
     */
    public LockerSearchResponseDto searchLockers(LockerSearchRequestDto request) {
        log.info("Searching for lockers near coordinates: [{}, {}], radius={}km",
                request.userLat(), request.userLon(), request.radiusInKm());

        List<Locker> areaLockers = fetchLockersFromGrids(request.userLat(), request.userLon(), request.radiusInKm());

        if (areaLockers.isEmpty()) {
            log.info("No lockers found in the surrounding geospatial grids.");
            return new LockerSearchResponseDto(areaLockers, null);
        }

        enrichLockersWithMetrics(areaLockers, request.userLat(), request.userLon());

        List<Locker> filteredLockers = filterAndSortLockers(areaLockers, request);

        log.info("Found {} lockers matching criteria out of {} machines in the search sector.",
                filteredLockers.size(), areaLockers.size());

        return new LockerSearchResponseDto(filteredLockers, handleWeatherInfo(request));
    }

    /**
     * Retrieves lockers from the local cache based on geospatial grid keys.
     *
     * @param lat        the latitude of the center point
     * @param lon        the longitude of the center point
     * @param radiusInKm the search radius in kilometers
     * @return a list of {@link Locker} objects found in the grids covering the specified area
     */
    private List<Locker> fetchLockersFromGrids(Double lat, Double lon, Double radiusInKm) {
        int searchDepth = radiusInKm.intValue() + 1;
        List<String> targetGrids = GeoGridUtil.getNeighboringGridKeys(lat, lon, searchDepth);
        return localLockerCache.getLockersForGrids(targetGrids);
    }

    /**
     * Enriches a list of lockers with calculated metrics such as distance and reliability scores.
     *
     * @param lockers the list of {@link Locker} objects to enrich
     * @param userLat the user's latitude
     * @param userLon the user's longitude
     */
    private void enrichLockersWithMetrics(List<Locker> lockers, Double userLat, Double userLon) {
        lockers.forEach(locker -> {
            double distance = distanceCalculator.calculateDistanceInKm(
                    userLat, userLon,
                    locker.getLocation().getLatitude(), locker.getLocation().getLongitude()
            );
            locker.setDistance(Math.round(distance * 100.0) / 100.0);
            locker.setEasyAccessReliability(ReliabilityScorer.calculateEasyAccessScore(locker));
            locker.setStressFreeReliability(ReliabilityScorer.calculateStressFreeScore(locker));
        });
    }

    /**
     * Filters and sorts the list of lockers based on radius and thermal suitability.
     *
     * @param lockers the list of {@link Locker} objects to filter and sort
     * @param request the search request parameters
     * @return a sorted and filtered list of {@link Locker} objects
     */
    private List<Locker> filterAndSortLockers(List<Locker> lockers, LockerSearchRequestDto request) {
        Predicate<Locker> searchFilter = isWithinRadius(request.radiusInKm());

        if (request.thermoMode()) {
            searchFilter = searchFilter.and(isThermoFriendly());
        }

        return lockers.stream()
                .filter(searchFilter)
                .sorted(Comparator.comparing(Locker::getDistance))
                .toList();
    }
    /**
     * Retrieves weather information for the requested delivery date and location.
     * <p>
     * If the expected delivery date is not provided, or if the weather service call fails,
     * it returns {@code null} and logs a warning.
     * </p>
     *
     * @param request the search request parameters containing location and delivery date
     * @return a {@link WeatherInfoDto} if successful, or {@code null} if unavailable
     */
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
