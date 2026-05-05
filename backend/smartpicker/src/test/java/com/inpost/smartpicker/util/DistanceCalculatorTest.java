package com.inpost.smartpicker.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class DistanceCalculatorTest {

    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    @Test
    void shouldReturnZeroWhenCoordinatesAreSame() {
        // given
        double lat = 52.2297;
        double lon = 21.0122;

        // when
        double distance = distanceCalculator.calculateDistanceInKm(lat, lon, lat, lon);

        // then
        assertThat(distance).isZero();
    }

    @Test
    void shouldCalculateCorrectDistanceBetweenTwoPoints() {
        // given
        // Warsaw
        double lat1 = 52.2297;
        double lon1 = 21.0122;
        // Krakow
        double lat2 = 50.0647;
        double lon2 = 19.9450;

        // when
        double distance = distanceCalculator.calculateDistanceInKm(lat1, lon1, lat2, lon2);

        // then
        // Approximately 252 km
        assertThat(distance).isCloseTo(252.0, offset(1.0));
    }

    @Test
    void shouldHandleLargeDistances() {
        // given
        // London
        double lat1 = 51.5074;
        double lon1 = -0.1278;
        // New York
        double lat2 = 40.7128;
        double lon2 = -74.0060;

        // when
        double distance = distanceCalculator.calculateDistanceInKm(lat1, lon1, lat2, lon2);

        // then
        // Approximately 5570 km
        assertThat(distance).isCloseTo(5570.0, offset(20.0));
    }

    @Test
    void shouldBeSymmetric() {
        // given
        double lat1 = 52.2297;
        double lon1 = 21.0122;
        double lat2 = 50.0647;
        double lon2 = 19.9450;

        // when
        double dist1 = distanceCalculator.calculateDistanceInKm(lat1, lon1, lat2, lon2);
        double dist2 = distanceCalculator.calculateDistanceInKm(lat2, lon2, lat1, lon1);

        // then
        assertThat(dist1).isEqualTo(dist2);
    }
}
