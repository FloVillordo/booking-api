package com.island.reservationapi.service;

import com.island.reservationapi.repository.CalendarAvailableRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final CalendarAvailableRepository calendarAvailableRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(AvailabilityService.class);

    /**
     * Returns a list of days available in the given range days.
     * If from or to are available will be contains in the response as appropriate
     *
     * @param from Arrival day
     * @param to   Departure day
     * @return List<LocalDate>
     */
    public List<LocalDate> getAvailableDates(@Future final LocalDate from, @Future final LocalDate to) {
        LOGGER.info("Getting availability for range of days from: {} to: {}", from, to);
        List<LocalDate> bookedDays = this.calendarAvailableRepository.getOrderedBookedDates(from, to);
        List<LocalDate> periodDays = from.datesUntil(to.plusDays(1)).collect(Collectors.toList());
        return periodDays.stream().filter(Predicate.not(bookedDays::contains)).collect(Collectors.toList());
    }
}
