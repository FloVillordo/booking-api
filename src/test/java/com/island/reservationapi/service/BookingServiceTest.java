package com.island.reservationapi.service;

import com.island.reservationapi.BookingApiApplication;
import com.island.reservationapi.exception.BookingNotFoundException;
import com.island.reservationapi.exception.CancelledBookingException;
import com.island.reservationapi.exception.UnavailableDatesException;
import com.island.reservationapi.model.Booking;
import com.island.reservationapi.model.BookingStatus;
import com.island.reservationapi.repository.BookingRepository;
import com.island.reservationapi.repository.CalendarAvailableRepository;
import com.island.reservationapi.request.CreateBookingControllerRequest;
import com.island.reservationapi.request.UpdateBookingControllerRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = BookingApiApplication.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BookingServiceTest {

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private CalendarAvailableRepository calendarAvailableRepository;

    @Autowired
    private BookingService bookingService;

    /**
     * Create a booking with unavailable dates
     */
    @Test
    public void whenUnavailableDates_thenBookingServiceShouldFail() {

        Mockito.when(this.calendarAvailableRepository.getBookedDates(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)))
                .thenReturn(Arrays.asList(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));
        CreateBookingControllerRequest request = new CreateBookingControllerRequest("Pepito", "pepito@gmail.com", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        Exception exception = Assert.assertThrows(UnavailableDatesException.class, () -> {
            this.bookingService.createBooking(request);
        });
        String expectedMessage = String.format("Days not available: [%s, %s] ", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Create a booking with available dates
     */
    @Test
    public void whenAvailableDates_thenBookingServiceCreateBooking() {

        String userName = "Pepito Juarez";
        String userEmail = "pepito@gmail.com";
        LocalDate arrivalDate = LocalDate.now().plusDays(5);
        LocalDate departureDate = LocalDate.now().plusDays(6);
        Booking booking = new Booking(userName, userEmail, arrivalDate, departureDate);
        Mockito.when(this.bookingRepository.save(booking)).thenReturn(booking);
        CreateBookingControllerRequest request = new CreateBookingControllerRequest(userName, userEmail, arrivalDate, departureDate);
        Booking b = this.bookingService.createBooking(request);
        Assert.assertEquals(b.getUserName(), userName);
        Assert.assertEquals(b.getUserEmail(), userEmail);
        Assert.assertEquals(b.getArrivalDate(), arrivalDate);
        Assert.assertEquals(b.getDepartureDate(), departureDate);
        Assert.assertEquals(b.getStatus(), BookingStatus.ACTIVE.getId());
    }

    /**
     * Cancel a booking that is already cancelled
     */
    @Test
    public void whenCancelACancelledBooking_thenBookingServiceShouldFail() {
        long bookingId = 1;
        Booking cancelledBooking = new Booking("FLor", "florencia@gmail.com", LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
        cancelledBooking.setStatus(1);
        Mockito.when(this.bookingRepository.findById(bookingId)).thenReturn(Optional.of(cancelledBooking));
        Exception exception = Assert.assertThrows(CancelledBookingException.class, () -> {
            this.bookingService.cancelBooking(bookingId);
        });
        String expectedMessage = String.format("Booking is cancelled cannot be modified");
        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Cancel a non existent booking
     */
    @Test
    public void whenCancelNonExistentBooking_thenBookingServiceShouldFail() {
        long bookingId = 2;
        Exception exception = Assert.assertThrows(BookingNotFoundException.class, () -> {
            this.bookingService.cancelBooking(bookingId);
        });
        String expectedMessage = String.format("Booking not found");
        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Update an non existent booking
     */
    @Test
    public void whenUpdateNonExistentBooking_thenBookingServiceShouldFail() {
        long bookingId = 2;
        LocalDate arrivalDate = LocalDate.now().plusDays(5);
        LocalDate departureDate = LocalDate.now().plusDays(6);
        UpdateBookingControllerRequest request = new UpdateBookingControllerRequest(null, null, arrivalDate, departureDate);
        Exception exception = Assert.assertThrows(BookingNotFoundException.class, () -> {
            this.bookingService.updateBooking(request, bookingId);
        });
        String expectedMessage = String.format("Booking not found");
        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Update an existent booking with available dates
     */
    @Test
    public void whenUpdateExitingBooking_thenBookingShouldUpdateTest() {
        long bookingId = 3;
        String userName = "Pepito Juarez";
        String userEmail = "pepito@gmail.com";
        LocalDate arrivalDate = LocalDate.now().plusDays(5);
        LocalDate departureDate = LocalDate.now().plusDays(6);
        Booking booking = new Booking(userName, userEmail, arrivalDate, departureDate);
        booking.setId(bookingId);
        Mockito.when(this.bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        LocalDate upDateArrivalDate = LocalDate.now().plusDays(7);
        LocalDate upDateDepartureDate = LocalDate.now().plusDays(8);
        Mockito.when(this.calendarAvailableRepository.getBookedDates(upDateArrivalDate, upDateDepartureDate)).thenReturn(Collections.emptyList());
        Booking updatedBooking = new Booking(userName, userEmail, upDateArrivalDate, upDateDepartureDate);
        Mockito.when(this.bookingRepository.save(updatedBooking)).thenReturn(updatedBooking);
        UpdateBookingControllerRequest request = new UpdateBookingControllerRequest(null, null, upDateArrivalDate, upDateDepartureDate);
        Booking responseBooking = this.bookingService.updateBooking(request, bookingId);
        Assert.assertEquals(responseBooking.getUserName(), userName);
        Assert.assertEquals(responseBooking.getUserEmail(), userEmail);
        Assert.assertEquals(responseBooking.getArrivalDate(), upDateArrivalDate);
        Assert.assertEquals(responseBooking.getDepartureDate(), upDateDepartureDate);
    }

    /**
     * Update an existent booking with unavailable dates
     */
    @Test
    public void whenUpdateExitingBookingWithUnavailableDate_thenBookingShouldUpdateTest() {
        long bookingId = 3;
        String userName = "Pepito Juarez";
        String userEmail = "pepito@gmail.com";
        LocalDate arrivalDate = LocalDate.now().plusDays(5);
        LocalDate departureDate = LocalDate.now().plusDays(6);
        Booking booking = new Booking(userName, userEmail, arrivalDate, departureDate);
        booking.setId(bookingId);
        Mockito.when(this.bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        LocalDate upDateArrivalDate = LocalDate.now().plusDays(7);
        LocalDate upDateDepartureDate = LocalDate.now().plusDays(8);
        Mockito.when(this.calendarAvailableRepository.getBookedDates(upDateArrivalDate, upDateDepartureDate)).thenReturn(Arrays.asList(LocalDate.now().plusDays(7)));
        Booking updatedBooking = new Booking(userName, userEmail, upDateArrivalDate, upDateDepartureDate);
        Mockito.when(this.bookingRepository.save(updatedBooking)).thenReturn(updatedBooking);
        UpdateBookingControllerRequest request = new UpdateBookingControllerRequest(null, null, upDateArrivalDate, upDateDepartureDate);
        Exception exception = Assert.assertThrows(UnavailableDatesException.class, () -> {
            this.bookingService.updateBooking(request, bookingId);
        });
        String expectedMessage = String.format("Days not available: [%s] ", LocalDate.now().plusDays(7));
        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

}
