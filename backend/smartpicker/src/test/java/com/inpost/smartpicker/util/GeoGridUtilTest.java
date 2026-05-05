package com.inpost.smartpicker.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class GeoGridUtilTest {

    @Test
    void shouldReturnCorrectGridKey() {
        // given
        double lat = 52.2297;
        double lon = 21.0122;

        // when
        String key = GeoGridUtil.getGridKey(lat, lon);

        // then
        // 52.2297 / 0.01 = 5222.97 -> rounded to 5223
        // 21.0122 / 0.016 = 1313.2625 -> rounded to 1313
        assertThat(key).isEqualTo("5223:1313");
    }

    @Test
    void shouldReturnNeighboringGridKeysForDepthZero() {
        // given
        double lat = 52.2297;
        double lon = 21.0122;
        int depth = 0;

        // when
        List<String> keys = GeoGridUtil.getNeighboringGridKeys(lat, lon, depth);

        // then
        assertThat(keys).containsExactly("5223:1313");
    }

    @Test
    void shouldReturnNeighboringGridKeysForDepthOne() {
        // given
        double lat = 52.2297;
        double lon = 21.0122;
        int depth = 1;

        // when
        List<String> keys = GeoGridUtil.getNeighboringGridKeys(lat, lon, depth);

        // then
        // (2*1 + 1)^2 = 9 keys
        assertThat(keys).hasSize(9);
        assertThat(keys).contains(
                "5222:1312", "5222:1313", "5222:1314",
                "5223:1312", "5223:1313", "5223:1314",
                "5224:1312", "5224:1313", "5224:1314"
        );
    }

    @Test
    void shouldHandleNegativeCoordinates() {
        // given
        double lat = -33.8688; // Sydney
        double lon = 151.2093;

        // when
        String key = GeoGridUtil.getGridKey(lat, lon);

        // then
        // -33.8688 / 0.01 = -3386.88 -> -3387
        // 151.2093 / 0.016 = 9450.58 -> 9451
        assertThat(key).isEqualTo("-3387:9451");
    }
}
