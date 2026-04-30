package com.inpost.smartpicker.service;

import com.inpost.smartpicker.exception.InPostApiException;
import com.inpost.smartpicker.model.InPostResponse;
import com.inpost.smartpicker.model.Locker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class InPostService {
    private final RestTemplate restTemplate;

    public InPostService() {
        this.restTemplate = new RestTemplate();
    }

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

}
