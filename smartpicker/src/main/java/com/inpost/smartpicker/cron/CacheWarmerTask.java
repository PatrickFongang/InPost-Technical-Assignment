package com.inpost.smartpicker.cron;

import com.inpost.smartpicker.service.InPostApiClient;
import com.inpost.smartpicker.service.InPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmerTask {

    private final InPostApiClient inPostApiClient;
    private final CacheManager cacheManager;

    private static final List<String> TOP_CITIES = List.of(
            "Warszawa", "London", "Berlin", "Kraków", "Paris", "Roma", "Wien",
            "Budapest", "Wrocław", "Poznań", "Łódź", "TEST", "Madrid", "MADRID", "Gdańsk",
            "Birmingham", "PARIS", "Milano", "Manchester", "Hamburg", "LONDON", "Marseille",
            "Szczecin", "Glasgow", "Barcelona", "BARCELONA", "Bydgoszcz", "Koeln", "Katowice",
            "Lublin", "Białystok", "Napoli", "Leeds", "Nottingham", "Torino", "HELSINKI",
            "Liverpool", "Frankfurt am Main", "Gdynia", "Bristol", "Sheffield", "Muenchen",
            "Toulouse", "Leicester", "Lyon", "GÖTEBORG", "Rzeszów", "Częstochowa", "Toruń", "Gliwice"
    );

    @Scheduled(cron = "0 0 3 * * ?")
    public void warmUpLockerCache() {
        log.info("Starting nightly cache warming for parcel lockers..");

        for (String city : TOP_CITIES) {
            try {
                Objects.requireNonNull(cacheManager.getCache("lockersByCity")).evict(city.toLowerCase());

                inPostApiClient.fetchLockersByCity(city);
                log.info("Cache for city {} updated.", city);

                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("Error warming up cache for city {}: {}", city, e.getMessage());
            }
        }

        log.info("Cache warming complete. System ready for morning traffic!");
    }
}