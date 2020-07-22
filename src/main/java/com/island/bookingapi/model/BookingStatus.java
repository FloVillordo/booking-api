package com.island.bookingapi.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum BookingStatus {
    ACTIVE(0),
    CANCELLED(1);

    Integer id;

    BookingStatus(final Integer id) {
        this.id = id;
    }

    public static BookingStatus getBookingStatusById(Integer id) {
        if (id != null) {
            Optional<BookingStatus> bookingStatus = Arrays.stream(BookingStatus.values()).filter(v -> v.getId().equals(id)).findFirst();
            if (Arrays.stream(BookingStatus.values()).anyMatch(v -> v.getId().equals(id))) {
                return bookingStatus.get();
            }
        }
        return null;
    }
}
