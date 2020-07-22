package com.island.reservationapi.service;


import com.island.reservationapi.BookingApiApplication;
import com.island.reservationapi.repository.BookingRepository;
import com.island.reservationapi.repository.CalendarAvailableRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BookingApiApplication.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class AvailabilityServiceTest {

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private CalendarAvailableRepository calendarAvailableRepository;

    @Autowired
    private AvailabilityService availabilityService;

    /**
     * Check available range of dates
     */
    @Test
    public void whenAvailableDates_thenReturnAllDates() {
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(25);
        Mockito.when(this.calendarAvailableRepository.getBookedDates(from, to))
                .thenReturn(Collections.emptyList());
        List<LocalDate> availableDates = this.availabilityService.getAvailableDates(from, to);
        Assert.assertEquals(availableDates, from.datesUntil(to.plusDays(1)).collect(Collectors.toList()));
    }

    @Test
    public void whenSomeUnavailableDate_thenReturnOnlyAvailableDates() {
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(25);
        Mockito.when(this.calendarAvailableRepository.getOrderedBookedDates(from, to))
                .thenReturn(Arrays.asList(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)));
        List<LocalDate> availableDatesResponse = this.availabilityService.getAvailableDates(from, to);
        List<LocalDate> availableDates = from.datesUntil(to.plusDays(1)).collect(Collectors.toList());
        availableDates.remove(LocalDate.now().plusDays(2));
        availableDates.remove(LocalDate.now().plusDays(3));
        Assert.assertEquals(availableDatesResponse, availableDates);
    }


}
