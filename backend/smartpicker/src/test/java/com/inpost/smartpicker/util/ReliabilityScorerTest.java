package com.inpost.smartpicker.util;

import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.model.enums.Reliability;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ReliabilityScorerTest {

    @Test
    void calculateEasyAccessScore_shouldReturnHighForPokOrPop() {
        // given
        Locker pokLocker = new Locker();
        pokLocker.setType(List.of("pok"));
        
        Locker popLocker = new Locker();
        popLocker.setType(List.of("pop"));

        // when & then
        assertThat(ReliabilityScorer.calculateEasyAccessScore(pokLocker)).isEqualTo(Reliability.HIGH);
        assertThat(ReliabilityScorer.calculateEasyAccessScore(popLocker)).isEqualTo(Reliability.HIGH);
    }

    @Test
    void calculateEasyAccessScore_shouldReturnNoneWhenNoEasyAccessZone() {
        // given
        Locker locker = new Locker();
        locker.setType(List.of("parcel_locker"));
        locker.setEasyAccessZone(false);

        // when
        Reliability result = ReliabilityScorer.calculateEasyAccessScore(locker);

        // then
        assertThat(result).isEqualTo(Reliability.NONE);
    }

    @Test
    void calculateEasyAccessScore_shouldReturnMediumWhenEasyAccessZoneAndLowInterest() {
        // given
        Locker locker = new Locker();
        locker.setType(List.of("parcel_locker"));
        locker.setEasyAccessZone(true);
        locker.setLowInterest(true);

        // when
        Reliability result = ReliabilityScorer.calculateEasyAccessScore(locker);

        // then
        assertThat(result).isEqualTo(Reliability.MEDIUM);
    }

    @Test
    void calculateEasyAccessScore_shouldReturnLowWhenEasyAccessZoneAndNotLowInterest() {
        // given
        Locker locker = new Locker();
        locker.setType(List.of("parcel_locker"));
        locker.setEasyAccessZone(true);
        locker.setLowInterest(false);

        // when
        Reliability result = ReliabilityScorer.calculateEasyAccessScore(locker);

        // then
        assertThat(result).isEqualTo(Reliability.LOW);
    }

    @Test
    void calculateStressFreeScore_shouldReturnHighForSuperPoints() {
        // given
        Locker pok = new Locker(); pok.setType(List.of("pok"));
        Locker pop = new Locker(); pop.setType(List.of("pop"));
        Locker superpop = new Locker(); superpop.setType(List.of("parcel_locker_superpop"));

        // when & then
        assertThat(ReliabilityScorer.calculateStressFreeScore(pok)).isEqualTo(Reliability.HIGH);
        assertThat(ReliabilityScorer.calculateStressFreeScore(pop)).isEqualTo(Reliability.HIGH);
        assertThat(ReliabilityScorer.calculateStressFreeScore(superpop)).isEqualTo(Reliability.HIGH);
    }

    @Test
    void calculateStressFreeScore_shouldReturnMediumForLowInterest() {
        // given
        Locker locker = new Locker();
        locker.setType(List.of("parcel_locker"));
        locker.setLowInterest(true);

        // when
        Reliability result = ReliabilityScorer.calculateStressFreeScore(locker);

        // then
        assertThat(result).isEqualTo(Reliability.MEDIUM);
    }

    @Test
    void calculateStressFreeScore_shouldReturnLowForOthers() {
        // given
        Locker locker = new Locker();
        locker.setType(List.of("parcel_locker"));
        locker.setLowInterest(false);

        // when
        Reliability result = ReliabilityScorer.calculateStressFreeScore(locker);

        // then
        assertThat(result).isEqualTo(Reliability.LOW);
    }
}
