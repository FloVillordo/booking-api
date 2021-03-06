package com.island.bookingapi.service;

import com.island.bookingapi.exception.BookingNotFoundException;
import com.island.bookingapi.exception.CancelledBookingException;
import com.island.bookingapi.exception.UnavailableDatesException;
import com.island.bookingapi.model.Booking;
import com.island.bookingapi.model.BookingStatus;
import com.island.bookingapi.model.CalendarAvailability;
import com.island.bookingapi.repository.BookingRepository;
import com.island.bookingapi.repository.CalendarAvailableRepository;
import com.island.bookingapi.request.CreateBookingControllerRequest;
import com.island.bookingapi.request.UpdateBookingControllerRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CalendarAvailableRepository calendarAvailableRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingService.class);


    /**
     * Create and returns a Booking If the days are available.
     * <p>
     * This method can be accessed concurrently, thread-safety
     * different thread can try to book the same days but only the
     * first its books the day will create the booking
     * all other transactions will be rolled back and will not be saved in the db
     *
     * @param request
     * @return Booking
     */
    @Transactional
    public Booking createBooking(CreateBookingControllerRequest request) {
        this.checkBookedDates(request.getArrivalDate(), request.getDepartureDate());
        // save booking
        Booking booking = new Booking(request.getUserName(), request.getUserEmail(), request.getArrivalDate(),
                request.getDepartureDate());
        LOGGER.info("Creating new booking for dates :{} - {} ", request.getArrivalDate(), request.getDepartureDate());
        this.saveBooking(booking);
        return booking;
    }

    /**
     * Update an existing booking
     * <p>
     * This method can be accessed concurrently, thread-safety
     * different thread can try to update different exiting booking for the same date but only the
     * first its books the date will update the booking
     * all other transactions will be rolled back and will not be saved in the db
     *
     * @param request
     * @param bookingId
     * @return Booking
     */
    @Transactional
    public Booking updateBooking(UpdateBookingControllerRequest request, Long bookingId) {
        Booking persistedBooking = this.getPersistedBooking(bookingId);
        this.checkBookedDates(request.getArrivalDate(), request.getDepartureDate(), persistedBooking);
        if (persistedBooking.getStatus().equals(BookingStatus.CANCELLED.getId())) {
            throw new CancelledBookingException();
        }
        this.cancelBookingDays(persistedBooking);
        String newUserName = request.getUserName();
        if (newUserName != null) {
            persistedBooking.setUserName(newUserName);
        }
        String newEmail = request.getUserEmail();
        if (newEmail != null) {
            persistedBooking.setUserEmail(newEmail);
        }
        persistedBooking.setArrivalDate(request.getArrivalDate());
        persistedBooking.setDepartureDate(request.getDepartureDate());
        // updateBooking booking
        LOGGER.info("Updating booking {} ", bookingId);
        this.saveBooking(persistedBooking);
        return persistedBooking;
    }

    /**
     * Get existing Booking by id
     *
     * @param bookingId
     * @return
     */
    public Booking getBookingById(Long bookingId) {
        return this.getPersistedBooking(bookingId);
    }

    /**
     * Cancel an exiting Booking and release the days on the calendar
     *
     * @param bookingId
     * @return
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking persistedBooking = this.getPersistedBooking(bookingId);
        if (persistedBooking.getStatus().equals(BookingStatus.CANCELLED.getId())) {
            throw new CancelledBookingException();
        }
        persistedBooking.setStatus(BookingStatus.CANCELLED.getId());
        LOGGER.info("Cancelling booking {} ", bookingId);
        this.cancelBookingDays(persistedBooking);
        return this.bookingRepository.save(persistedBooking);
    }

    private Booking getPersistedBooking(Long bookingId) {
        return this.bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);
    }

    private void checkBookedDates(LocalDate arrivalDate, LocalDate departureDate) {
        List<LocalDate> bookedDays = this.calendarAvailableRepository.getBookedDates(arrivalDate, departureDate);
        if (!bookedDays.isEmpty()) {
            throw new UnavailableDatesException(String.format("Days not available: %s ", bookedDays));
        }
    }

    private void checkBookedDates(LocalDate arrivalDate, LocalDate departureDate, Booking persistedBooking) {
        List<LocalDate> oldBookedDays = persistedBooking.getArrivalDate().datesUntil(persistedBooking.getDepartureDate()).collect(Collectors.toList());
        List<LocalDate> bookedDays = this.calendarAvailableRepository.getBookedDates(arrivalDate, departureDate);
        if (!bookedDays.isEmpty() && bookedDays.stream().noneMatch(oldBookedDays::contains)) {
            throw new UnavailableDatesException(String.format("Days not available: %s ", bookedDays));
        }
    }

    private void saveBooking(Booking booking) {
        List<LocalDate> bookingDates = booking.getArrivalDate().datesUntil(booking.getDepartureDate()).collect(Collectors.toList());
        List<CalendarAvailability> calendarAvailabilities = bookingDates.stream().map(CalendarAvailability::new).collect(Collectors.toList());
        this.calendarAvailableRepository.saveAll(calendarAvailabilities);
        this.bookingRepository.save(booking);

    }

    private void cancelBookingDays(Booking booking) {
        List<LocalDate> bookingDates = booking.getArrivalDate().datesUntil(booking.getDepartureDate()).collect(Collectors.toList());
        LOGGER.info("Setting calendar availability for Days: {} ", bookingDates);
        this.calendarAvailableRepository.deleteByDates(bookingDates);

    }


}
