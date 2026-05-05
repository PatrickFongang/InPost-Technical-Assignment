package com.inpost.smartpicker.predicate;

import com.inpost.smartpicker.model.AddressDetails;
import com.inpost.smartpicker.model.Location;
import com.inpost.smartpicker.model.Locker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockerDataCleaningPredicatesTest {

    @Test
    void shouldThrowExceptionWhenInstantiatingUtilityClass() throws NoSuchMethodException {
        Constructor<LockerDataCleaningPredicates> constructor = LockerDataCleaningPredicates.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void hasValidCoordinates_shouldReturnTrueWhenLatAndLonArePresent() {
        // given
        Locker locker = new Locker();
        Location location = new Location();
        location.setLatitude(52.0);
        location.setLongitude(21.0);
        locker.setLocation(location);

        // when & then
        assertThat(LockerDataCleaningPredicates.hasValidCoordinates().test(locker)).isTrue();
    }

    @Test
    void hasValidCoordinates_shouldReturnFalseWhenLocationIsNull() {
        // given
        Locker locker = new Locker();
        locker.setLocation(null);

        // when & then
        assertThat(LockerDataCleaningPredicates.hasValidCoordinates().test(locker)).isFalse();
    }

    @Test
    void hasValidCoordinates_shouldReturnFalseWhenLatOrLonIsNull() {
        // given
        Locker locker = new Locker();
        Location location = new Location();
        locker.setLocation(location);

        // when & then
        location.setLatitude(null);
        location.setLongitude(21.0);
        assertThat(LockerDataCleaningPredicates.hasValidCoordinates().test(locker)).isFalse();

        location.setLatitude(52.0);
        location.setLongitude(null);
        assertThat(LockerDataCleaningPredicates.hasValidCoordinates().test(locker)).isFalse();
    }

    @Test
    void isNotTestMachine_shouldReturnTrueWhenCityDoesNotContainTestKeywords() {
        // given
        Locker locker = new Locker();
        AddressDetails details = new AddressDetails();
        details.setCity("Warszawa");
        locker.setAddressDetails(details);

        // when & then
        assertThat(LockerDataCleaningPredicates.isNotTestMachine().test(locker)).isTrue();
    }

    @Test
    void isNotTestMachine_shouldReturnFalseWhenCityContainsTest() {
        // given
        Locker locker = new Locker();
        AddressDetails details = new AddressDetails();
        locker.setAddressDetails(details);

        details.setCity("WARSZAWA TEST");
        assertThat(LockerDataCleaningPredicates.isNotTestMachine().test(locker)).isFalse();

        details.setCity("test_city");
        assertThat(LockerDataCleaningPredicates.isNotTestMachine().test(locker)).isFalse();
    }

    @Test
    void isNotTestMachine_shouldReturnFalseWhenCityContainsDoWykorszystania() {
        // given
        Locker locker = new Locker();
        AddressDetails details = new AddressDetails();
        details.setCity("Kraków DO WYKORZYSTANIA");
        locker.setAddressDetails(details);

        // when & then
        assertThat(LockerDataCleaningPredicates.isNotTestMachine().test(locker)).isFalse();
    }

    @Test
    void isNotTestMachine_shouldReturnTrueWhenAddressDetailsOrCityIsNull() {
        // given
        Locker locker = new Locker();

        // case 1: details null
        locker.setAddressDetails(null);
        assertThat(LockerDataCleaningPredicates.isNotTestMachine().test(locker)).isTrue();

        // case 2: city null
        locker.setAddressDetails(new AddressDetails());
        assertThat(LockerDataCleaningPredicates.isNotTestMachine().test(locker)).isTrue();
    }
}
