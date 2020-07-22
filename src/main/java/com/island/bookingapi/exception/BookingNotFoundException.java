package com.island.bookingapi.exception;


public class BookingNotFoundException extends RuntimeException {
    private static final String BOOKING_NOT_FOUND = "Booking not found";

    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException() {
        super(BOOKING_NOT_FOUND);
    }

    public BookingNotFoundException(Throwable cause) {
        super(BOOKING_NOT_FOUND, cause);
    }

    public BookingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
