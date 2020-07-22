package com.island.bookingapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class AvailabilityDTO {

    private LocalDate from;
    private LocalDate to;
    private List<LocalDate> availableDates;


}
