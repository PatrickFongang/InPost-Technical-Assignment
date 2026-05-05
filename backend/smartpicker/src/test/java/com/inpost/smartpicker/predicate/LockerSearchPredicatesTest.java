package com.inpost.smartpicker.predicate;

import com.inpost.smartpicker.model.Locker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockerSearchPredicatesTest {

    @Test
    void shouldThrowExceptionWhenInstantiatingUtilityClass() throws NoSuchMethodException {
        Constructor<LockerSearchPredicates> constructor = LockerSearchPredicates.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void isWithinRadius_shouldReturnTrueWhenDistanceIsWithinLimit() {
        // given
        Locker locker = new Locker();
        locker.setDistance(1.5);

        // when & then
        assertThat(LockerSearchPredicates.isWithinRadius(2.0).test(locker)).isTrue();
        assertThat(LockerSearchPredicates.isWithinRadius(1.5).test(locker)).isTrue();
    }

    @Test
    void isWithinRadius_shouldReturnFalseWhenDistanceIsOutsideLimit() {
        // given
        Locker locker = new Locker();
        locker.setDistance(2.1);

        // when & then
        assertThat(LockerSearchPredicates.isWithinRadius(2.0).test(locker)).isFalse();
    }

    @Test
    void isWithinRadius_shouldReturnFalseWhenDistanceIsNull() {
        // given
        Locker locker = new Locker();
        locker.setDistance(null);

        // when & then
        assertThat(LockerSearchPredicates.isWithinRadius(2.0).test(locker)).isFalse();
    }

    @Test
    void isThermoFriendly_shouldReturnTrueWhenLocationTypeIsIndoor() {
        // given
        Locker locker = new Locker();
        locker.setLocationType("Indoor");

        // when & then
        assertThat(LockerSearchPredicates.isThermoFriendly().test(locker)).isTrue();
        
        locker.setLocationType("INDOOR");
        assertThat(LockerSearchPredicates.isThermoFriendly().test(locker)).isTrue();
    }

    @Test
    void isThermoFriendly_shouldReturnFalseWhenLocationTypeIsNotIndoor() {
        // given
        Locker locker = new Locker();
        
        locker.setLocationType("Outdoor");
        assertThat(LockerSearchPredicates.isThermoFriendly().test(locker)).isFalse();

        locker.setLocationType(null);
        assertThat(LockerSearchPredicates.isThermoFriendly().test(locker)).isFalse();
    }
}
