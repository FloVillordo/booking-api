package com.island.bookingapi.exception;

public class UnavailableDatesException extends RuntimeException {
    private static final String UNAVAILABLE_DATES_ERROR_MSG = "Some days are not available";

    public UnavailableDatesException(String message) {
        super(message);
    }

    public UnavailableDatesException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnavailableDatesException() {
        super(UNAVAILABLE_DATES_ERROR_MSG);
    }

    public UnavailableDatesException(Throwable cause) {
        super(UNAVAILABLE_DATES_ERROR_MSG, cause);
    }

}
