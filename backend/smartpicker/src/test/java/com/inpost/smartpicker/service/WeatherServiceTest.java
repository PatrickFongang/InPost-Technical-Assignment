package com.inpost.smartpicker.service;

import com.inpost.smartpicker.dto.weather.OpenMeteoResponse;
import com.inpost.smartpicker.dto.weather.WeatherInfoDto;
import com.inpost.smartpicker.exception.WeatherApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void shouldReturnWeatherInfoWhenApiRespondsCorrectly() {
        // given
        double lat = 52.0, lon = 21.0;
        LocalDate date = LocalDate.now();
        OpenMeteoResponse mockResponse = createMockResponse(10.0, 20.0);

        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class))).thenReturn(mockResponse);

        // when
        WeatherInfoDto result = weatherService.getWeatherForecast(lat, lon, date);

        // then
        assertThat(result.isExtreme()).isFalse();
        assertThat(result.minTemp()).isEqualTo(10.0);
        assertThat(result.maxTemp()).isEqualTo(20.0);
    }

    @Test
    void shouldIdentifyExtremeColdWeather() {
        // given
        OpenMeteoResponse mockResponse = createMockResponse(4.9, 15.0);
        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class))).thenReturn(mockResponse);

        // when
        WeatherInfoDto result = weatherService.getWeatherForecast(52.0, 21.0, LocalDate.now());

        // then
        assertThat(result.isExtreme()).isTrue();
    }

    @Test
    void shouldIdentifyExtremeHotWeather() {
        // given
        OpenMeteoResponse mockResponse = createMockResponse(10.0, 25.1);
        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class))).thenReturn(mockResponse);

        // when
        WeatherInfoDto result = weatherService.getWeatherForecast(52.0, 21.0, LocalDate.now());

        // then
        assertThat(result.isExtreme()).isTrue();
    }

    @Test
    void shouldThrowWeatherApiExceptionWhenResponseIsNull() {
        // given
        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class))).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> weatherService.getWeatherForecast(52.0, 21.0, LocalDate.now()))
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("empty or malformed");
    }

    @Test
    void shouldThrowWeatherApiExceptionWhenDailyDataIsMissing() {
        // given
        OpenMeteoResponse mockResponse = new OpenMeteoResponse();
        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class))).thenReturn(mockResponse);

        // when & then
        assertThatThrownBy(() -> weatherService.getWeatherForecast(52.0, 21.0, LocalDate.now()))
                .isInstanceOf(WeatherApiException.class);
    }

    @Test
    void shouldThrowWeatherApiExceptionWhenRestClientFails() {
        // given
        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        // when & then
        assertThatThrownBy(() -> weatherService.getWeatherForecast(52.0, 21.0, LocalDate.now()))
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("Failed to communicate");
    }

    private OpenMeteoResponse createMockResponse(double min, double max) {
        OpenMeteoResponse response = new OpenMeteoResponse();
        OpenMeteoResponse.Daily daily = new OpenMeteoResponse.Daily();
        daily.setTemperatureMin(List.of(min));
        daily.setTemperatureMax(List.of(max));
        response.setDaily(daily);
        return response;
    }
}
