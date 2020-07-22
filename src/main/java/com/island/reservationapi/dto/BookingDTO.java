package com.island.reservationapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.island.reservationapi.model.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BookingDTO {

    private Long bookingId;

    private String userName;

    private String userEmail;

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    private BookingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
