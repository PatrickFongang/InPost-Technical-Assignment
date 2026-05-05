package com.inpost.smartpicker.service;

import com.inpost.smartpicker.dto.search.LockerSearchRequestDto;
import com.inpost.smartpicker.dto.search.LockerSearchResponseDto;
import com.inpost.smartpicker.dto.weather.WeatherInfoDto;
import com.inpost.smartpicker.exception.WeatherApiException;
import com.inpost.smartpicker.model.Location;
import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.util.DistanceCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InPostServiceTest {

    @Mock
    private LocalLockerCache localLockerCache;

    @Mock
    private DistanceCalculator distanceCalculator;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private InPostService inPostService;

    @Test
    void shouldReturnEmptyResponseWhenNoLockersFoundInGrids() {
        // given
        LockerSearchRequestDto request = new LockerSearchRequestDto(52.0, 21.0, 5.0, null, false);
        when(localLockerCache.getLockersForGrids(anyList())).thenReturn(Collections.emptyList());

        // when
        LockerSearchResponseDto response = inPostService.searchLockers(request);

        // then
        assertThat(response.lockers()).isEmpty();
        assertThat(response.weatherInfo()).isNull();

        verifyNoInteractions(distanceCalculator);
    }

    @Test
    void shouldEnrichFilterAndSortLockersByDistance() {
        // given
        LockerSearchRequestDto request = new LockerSearchRequestDto(52.0, 21.0, 5.0, null, false);

        Locker locker1 = createMockLocker("Locker1", 52.1, 21.1);
        Locker locker2 = createMockLocker("Locker2", 52.2, 21.2);
        Locker locker3 = createMockLocker("LockerOut", 53.0, 22.0);

        when(localLockerCache.getLockersForGrids(anyList())).thenReturn(List.of(locker1, locker2, locker3));

        when(distanceCalculator.calculateDistanceInKm(52.0, 21.0, 52.1, 21.1)).thenReturn(2.0);
        when(distanceCalculator.calculateDistanceInKm(52.0, 21.0, 52.2, 21.2)).thenReturn(4.5);
        when(distanceCalculator.calculateDistanceInKm(52.0, 21.0, 53.0, 22.0)).thenReturn(10.0);

        // when
        LockerSearchResponseDto response = inPostService.searchLockers(request);

        // then
        assertThat(response.lockers()).hasSize(2);
        assertThat(response.lockers().get(0).getName()).isEqualTo("Locker1");
        assertThat(response.lockers().get(1).getName()).isEqualTo("Locker2");
        assertThat(response.lockers().get(0).getDistance()).isEqualTo(2.0);
    }

    @Test
    void shouldFetchWeatherInfoWhenDeliveryDateIsProvided() {
        // given
        LocalDate deliveryDate = LocalDate.now();
        LockerSearchRequestDto request = new LockerSearchRequestDto(52.0, 21.0, 5.0, deliveryDate, false);

        Locker locker = createMockLocker("Locker1", 52.1, 21.1);
        when(localLockerCache.getLockersForGrids(anyList())).thenReturn(List.of(locker));
        when(distanceCalculator.calculateDistanceInKm(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(1.0);

        WeatherInfoDto mockWeather = new WeatherInfoDto(true, -5.0, -1.0);
        when(weatherService.getWeatherForecast(52.0, 21.0, deliveryDate)).thenReturn(mockWeather);

        // when
        LockerSearchResponseDto response = inPostService.searchLockers(request);

        // then
        assertThat(response.weatherInfo()).isNotNull();
        assertThat(response.weatherInfo().isExtreme()).isTrue();
        assertThat(response.weatherInfo().minTemp()).isEqualTo(-5.0);
        verify(weatherService).getWeatherForecast(52.0, 21.0, deliveryDate);
    }

    @Test
    void shouldReturnNullWeatherInfoWhenWeatherServiceThrowsException() {
        // given
        LocalDate deliveryDate = LocalDate.now();
        LockerSearchRequestDto request = new LockerSearchRequestDto(52.0, 21.0, 5.0, deliveryDate, false);

        Locker locker = createMockLocker("Locker1", 52.1, 21.1);
        when(localLockerCache.getLockersForGrids(anyList())).thenReturn(List.of(locker));
        when(distanceCalculator.calculateDistanceInKm(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(1.0);

        when(weatherService.getWeatherForecast(52.0, 21.0, deliveryDate))
                .thenThrow(new WeatherApiException("API is down"));

        // when
        LockerSearchResponseDto response = inPostService.searchLockers(request);

        // then
        assertThat(response.weatherInfo()).isNull();
        assertThat(response.lockers()).hasSize(1);
    }

    @Test
    void shouldFilterThermoFriendlyLockers() {
        // given
        LockerSearchRequestDto request = new LockerSearchRequestDto(52.0, 21.0, 5.0, null, true);

        Locker indoorLocker = createMockLocker("IndoorLocker", 52.1, 21.1);
        indoorLocker.setLocationType("Indoor");
        Locker outdoorLocker = createMockLocker("OutdoorLocker", 52.2, 21.2);
        outdoorLocker.setLocationType("Outdoor");

        when(localLockerCache.getLockersForGrids(anyList())).thenReturn(List.of(indoorLocker, outdoorLocker));
        when(distanceCalculator.calculateDistanceInKm(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(1.0);

        // when
        LockerSearchResponseDto response = inPostService.searchLockers(request);

        // then
        assertThat(response.lockers()).hasSize(1);
        assertThat(response.lockers().get(0).getName()).isEqualTo("IndoorLocker");
    }

    @Test
    void shouldCalculateReliabilityScoresWhenEnriching() {
        // given
        LockerSearchRequestDto request = new LockerSearchRequestDto(52.0, 21.0, 5.0, null, false);
        Locker locker = createMockLocker("Locker1", 52.1, 21.1);

        
        when(localLockerCache.getLockersForGrids(anyList())).thenReturn(List.of(locker));
        when(distanceCalculator.calculateDistanceInKm(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(1.5);

        // when
        LockerSearchResponseDto response = inPostService.searchLockers(request);

        // then
        assertThat(response.lockers()).hasSize(1);
        Locker enriched = response.lockers().get(0);
        assertThat(enriched.getDistance()).isEqualTo(1.5);
        assertThat(enriched.getEasyAccessReliability()).isNotNull();
        assertThat(enriched.getStressFreeReliability()).isNotNull();
    }

    @Test
    void shouldHandleLargeRadiusCorrectMapGrids() {
        // given
        LockerSearchRequestDto request = new LockerSearchRequestDto(52.0, 21.0, 10.0, null, false);
        // searchDepth will be 10 + 1 = 11
        
        when(localLockerCache.getLockersForGrids(anyList())).thenReturn(Collections.emptyList());

        // when
        inPostService.searchLockers(request);

        // then
        verify(localLockerCache).getLockersForGrids(argThat(list -> list.size() > 1));
    }


    private Locker createMockLocker(String name, double lat, double lon) {
        Locker locker = new Locker();
        locker.setName(name);
        Location location = new Location();
        location.setLatitude(lat);
        location.setLongitude(lon);
        locker.setLocation(location);
        locker.setLowInterest(false);
        locker.setEasyAccessZone(false);
        locker.setType(Collections.emptyList());
        return locker;
    }
}