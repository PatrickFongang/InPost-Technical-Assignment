package com.inpost.smartpicker.service;

import com.inpost.smartpicker.dto.weather.OpenMeteoResponse;
import com.inpost.smartpicker.dto.weather.WeatherInfoDto;
import com.inpost.smartpicker.exception.WeatherApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;
    private static final double MIN_SAFE_TEMP = 5.0;
    private static final double MAX_SAFE_TEMP = 25.0;

    /**
     * Fetches the weather forecast for the specified coordinates and date.
     * <p>
     * This method calls the Open-Meteo API to retrieve daily minimum and maximum temperatures.
     * It then determines if the weather is extreme based on predefined safe temperature thresholds.
     * </p>
     *
     * @param lat  the latitude of the location
     * @param lon  the longitude of the location
     * @param date the date for which the forecast is requested
     * @return a {@link WeatherInfoDto} containing information about whether the weather is extreme,
     *         and the retrieved minimum and maximum temperatures
     * @throws WeatherApiException if the API response is null, malformed, or if there is a communication error with the API
     */
    public WeatherInfoDto getWeatherForecast(double lat, double lon, LocalDate date) {
        log.info("Fetching weather forecast for coordinates: [{}, {}] on date: {}", lat, lon, date);

        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&start_date=%s&end_date=%s" +
                        "&daily=temperature_2m_max,temperature_2m_min",
                lat, lon, date, date
        );

        try {
            OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);

            if (response == null || response.getDaily() == null
                    || response.getDaily().getTemperatureMin() == null || response.getDaily().getTemperatureMin().isEmpty()
                    || response.getDaily().getTemperatureMax() == null || response.getDaily().getTemperatureMax().isEmpty()) {
                throw new WeatherApiException("Open-Meteo API returned empty or malformed daily forecast data.");
            }

            double minTemp = response.getDaily().getTemperatureMin().getFirst();
            double maxTemp = response.getDaily().getTemperatureMax().getFirst();

            boolean isExtreme = minTemp < MIN_SAFE_TEMP || maxTemp > MAX_SAFE_TEMP;

            if (isExtreme) {
                log.info("Extreme weather detected! Min: {}°C, Max: {}°C", minTemp, maxTemp);
            }

            return new WeatherInfoDto(isExtreme, minTemp, maxTemp);

        } catch (RestClientException e) {
            throw new WeatherApiException("Failed to communicate with Open-Meteo API: " + e.getMessage(), e);
        }

    }
}