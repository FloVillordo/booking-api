package com.island.bookingapi.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRangeValidator implements ConstraintValidator<ValidateDateRange, Object> {
    private String arrivalDate;
    private String departureDate;
    private static final Integer MAX_STAY = 3;
    private static final Integer BOOKING_ANTICIPATION_DAYS = 30;

    @Override
    public void initialize(final ValidateDateRange constraintAnnotation) {
        this.arrivalDate = constraintAnnotation.arrivalDate();
        this.departureDate = constraintAnnotation.departureDate();
    }

    /**
     * Validates that the given dates range is valid
     *
     * @param value
     * @param context
     * @return {@code false} if {@code value} does not pass the constraint
     */
    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null)
            return true;

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);

        LocalDate arrival = (LocalDate) wrapper.getPropertyValue("arrivalDate");
        LocalDate departure = (LocalDate) wrapper.getPropertyValue("departureDate");
        if (arrival == null && departure == null) {
            return true;
        }

        if (arrival == null || departure == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Arrival and Departure should be indicated").addConstraintViolation();
            return false;
        }

        if (arrival.isAfter(departure) || arrival.isEqual(departure)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Arrival date must be greater than departure date").addConstraintViolation();
            return false;
        }

        if (ChronoUnit.DAYS.between(arrival, departure) > MAX_STAY) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Stay cannot be more than 3 days").addConstraintViolation();
            return false;
        }
        LocalDate now = LocalDate.now();
        if (now.compareTo(arrival) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Arrival date must be greater today").addConstraintViolation();
            return false;
        }
        LocalDate limitLocalDate = now.plusDays(BOOKING_ANTICIPATION_DAYS);
        if (limitLocalDate.compareTo(arrival) < 0) {

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format("Arrival date must be less than %s", limitLocalDate.toString())).addConstraintViolation();
            return false;
        }

        return true;

    }
}
