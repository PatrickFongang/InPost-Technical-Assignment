package com.inpost.smartpicker.service;

import com.inpost.smartpicker.model.InPostResponse;
import com.inpost.smartpicker.model.Locker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class InPostService {
    private final RestTemplate restTemplate;

    public InPostService() {
        this.restTemplate = new RestTemplate();
    }

    public List<Locker> fetchLockersByCity(String city) {
        String url = "https://api-global-points.easypack24.net/v1/points?city=" + city;

        try {
            InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);

            if (response != null && response.getItems() != null) {
                return response.getItems();
            }
        } catch (Exception e) {
            System.err.println("Błąd pobierania danych z InPost API: " + e.getMessage());
        }

        return Collections.emptyList();
    }

}
