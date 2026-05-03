package com.inpost.smartpicker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InPostResponse {
    private List<Locker> items;

    @JsonProperty("total_pages")
    private Integer totalPages;

    private Integer page;
}
