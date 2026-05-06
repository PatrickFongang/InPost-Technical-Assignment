package com.inpost.smartpicker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inpost.smartpicker.model.enums.Reliability;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Locker {
    private String name;

    private String country;

    private List<String> type;

    @JsonProperty("opening_hours")
    private String openingHours;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("easy_access_zone")
    private Boolean easyAccessZone;

    @JsonProperty("recommended_low_interest_box_machines_list")
    private List<String> recommendedLowInterestBoxMachinesList;

    private Location location;

    @JsonProperty("address_details")
    private AddressDetails addressDetails;

    @JsonProperty("locker_availability")
    private LockerAvailability lockerAvailability;

    private Double distance;

    private Reliability easyAccessReliability;
    private Reliability stressFreeReliability;

    private Boolean lowInterest;
}
