package com.inpost.smartpicker.service;

import com.inpost.smartpicker.exception.InPostApiException;
import com.inpost.smartpicker.model.InPostResponse;
import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        log.info("Rozpoczynam pobieranie paczkomatów dla miasta: {}", city);

        String url = "https://api-global-points.easypack24.net/v1/points?city=" + city;

        try {
            InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);

            if (response != null && response.getItems() != null) {
                log.info("Pomyślnie pobrano {} paczkomatów z InPost API.", response.getItems().size());
                return response.getItems();
            }
            log.warn("API InPost zwróciło pustą odpowiedź dla miasta: {}", city);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Wystąpił błąd podczas komunikacji z InPost API dla miasta {}. Szczegóły: {}", city, e.getMessage());
            throw new InPostApiException("Nie udało się pobrać danych z InPost API dla miasta: " + city);
        }
    }
    public List<Locker> searchLockers(String city, double userLat, double userLon, double radiusInKm, boolean stressFreeMode, boolean thermoMode) {
        log.info("Wyszukiwanie paczkomatów: miasto={}, radius={}, stressFree={}, thermo={}",
                city, radiusInKm, stressFreeMode, thermoMode);

        List<Locker> allLockers = fetchLockersByCity(city);

        allLockers.forEach(locker -> {
            if (locker.getLocation() != null && locker.getLocation().getLatitude() != null &&
                    locker.getLocation().getLongitude() != null) {
                double distance = distanceCalculator.calculateDistanceInKm(
                        userLat, userLon,
                        locker.getLocation().getLatitude(), locker.getLocation().getLongitude()
                );
                locker.setDistance(Math.round(distance * 100.0) / 100.0);
            }
        });

        Predicate<Locker> searchFilter = isWithinRadius(radiusInKm);

        if (stressFreeMode) {
            log.info("Dodaję filtr Trybu Bezstresowego");
            searchFilter = searchFilter.and(isStressFree());
        }

        if (thermoMode) {
            log.info("Dodaję filtr Trybu Termo-ochronnego");
            searchFilter = searchFilter.and(isThermoFriendly());
        }

        return allLockers.stream()
                .filter(searchFilter)
                .sorted(Comparator.comparing(Locker::getDistance))
                .toList();
    }

}
