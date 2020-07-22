package com.island.bookingapi.exception;

public class CancelledBookingException extends RuntimeException {

    private static final String CANCELLED_BOOKING_MSG = "Booking is cancelled cannot be modified";

    public CancelledBookingException(String message) {
        super(message);
    }


    public CancelledBookingException() {
        super(CANCELLED_BOOKING_MSG);
    }

    public CancelledBookingException(Throwable cause) {
        super(CANCELLED_BOOKING_MSG, cause);
    }

    public CancelledBookingException(String message, Throwable cause) {
        super(message, cause);
    }

}
