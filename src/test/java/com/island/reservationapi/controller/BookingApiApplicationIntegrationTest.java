package com.island.reservationapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.island.reservationapi.model.Booking;
import com.island.reservationapi.repository.BookingRepository;
import com.island.reservationapi.repository.CalendarAvailableRepository;
import com.island.reservationapi.request.CreateBookingControllerRequest;
import com.island.reservationapi.request.UpdateBookingControllerRequest;
import database.BookingPostgresqlContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BookingApiApplicationIntegrationTest {
    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = BookingPostgresqlContainer.getInstance();

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CalendarAvailableRepository calendarAvailableRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Before()
    public void setUp() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Test
    @Transactional
    public void whenAvailableDates_bookOk() throws Exception {
        int previousBookings = this.bookingRepository.findAll().size();
        String userName = "Pepito Juarez";
        String userEmail = "pepito@gmail.com";
        LocalDate arrivalDate = LocalDate.now().plusDays(10);
        LocalDate departureDate = LocalDate.now().plusDays(12);
        CreateBookingControllerRequest body = new CreateBookingControllerRequest(userName, userEmail, arrivalDate, departureDate);
        this.testPOSTMethod("/booking", body, 201);
        Assert.assertEquals(previousBookings + 1, this.bookingRepository.findAll().size());
    }

    @Test
    @Transactional
    public void whenInvalidEmail_bookFail() throws Exception {
        int previousBookings = this.bookingRepository.findAll().size();
        String userName = "Pepito Juarez";
        String userEmail = "pepitoilcom";
        LocalDate arrivalDate = LocalDate.now().plusDays(10);
        LocalDate departureDate = LocalDate.now().plusDays(12);
        CreateBookingControllerRequest body = new CreateBookingControllerRequest(userName, userEmail, arrivalDate, departureDate);
        this.testPOSTMethod("/booking", body, 400);
        Assert.assertEquals(previousBookings, this.bookingRepository.findAll().size());
    }

    @Test
    @Transactional
    public void whenBlankUserName_bookFail() throws Exception {
        int previousBookings = this.bookingRepository.findAll().size();
        String userName = "";
        String userEmail = "pepito@gmail.com";
        LocalDate arrivalDate = LocalDate.now().plusDays(10);
        LocalDate departureDate = LocalDate.now().plusDays(12);
        CreateBookingControllerRequest body = new CreateBookingControllerRequest(userName, userEmail, arrivalDate, departureDate);
        this.testPOSTMethod("/booking", body, 400);
        Assert.assertEquals(previousBookings, this.bookingRepository.findAll().size());
    }

    @Test
    @Transactional
    public void whenUnavailableDates_bookFail() throws Exception {
        int previousBookings = this.bookingRepository.findAll().size();
        String userName = "Pepito Juarez";
        String userEmail = "pepito@gmail.com";
        LocalDate arrivalDate = LocalDate.now().plusDays(2);
        LocalDate departureDate = LocalDate.now().plusDays(3);
        CreateBookingControllerRequest body = new CreateBookingControllerRequest(userName, userEmail, arrivalDate, departureDate);
        this.testPOSTMethod("/booking", body, 400);
        Assert.assertEquals(previousBookings, this.bookingRepository.findAll().size());
    }

    @Test
    @Transactional
    public void whenUnavailableRageOfDates_bookFail() throws Exception {
        int previousBookings = this.bookingRepository.findAll().size();
        String userName = "Pepito Juarez";
        String userEmail = "pepito@gmail.com";
        LocalDate arrivalDate = LocalDate.now().plusDays(10);
        LocalDate departureDate = LocalDate.now().plusDays(20);
        CreateBookingControllerRequest body = new CreateBookingControllerRequest(userName, userEmail, arrivalDate, departureDate);
        this.testPOSTMethod("/booking", body, 400);
        Assert.assertEquals(previousBookings, this.bookingRepository.findAll().size());
    }

    @Test
    public void whenGetAvailability_getAvailabilityOk() throws Exception {
        String from = LocalDate.now().plusDays(1).toString();
        String to = LocalDate.now().plusDays(10).toString();
        String url = String.format("/availability?from=%s&to=%s", from, to);
        this.testGETMethod(url, 200);
    }

    @Test
    public void whenGetAvailabilityInPastDates_Fail() throws Exception {
        String from = LocalDate.now().withYear(2019).toString();
        String to = LocalDate.now().withYear(2019).toString();
        String url = String.format("/availability?from=%s&to=%s", from, to);
        this.testGETMethod(url, 400);
    }

    @Test
    @Transactional
    public void whenCancelBooking_FreeCalendarDays() throws Exception {
        Booking booking = this.bookingRepository.findAll().stream().findFirst().get();
        LocalDate arrivalDate = booking.getArrivalDate();
        LocalDate departureDate = booking.getDepartureDate();
        Assert.assertTrue(this.calendarAvailableRepository.getBookedDates(arrivalDate, departureDate).size() > 0);
        String url = String.format("/booking/%s", booking.getId());
        this.testDELETEMethod(url, 200);
        Assert.assertEquals(0, this.calendarAvailableRepository.getBookedDates(arrivalDate, departureDate).size());

    }

    @Test
    @Transactional
    public void whenUpdateOverlappingDates_updateBookingOk() throws Exception {
        Booking booking = this.bookingRepository.findAll().stream().findFirst().get();
        LocalDate arrivalDate = booking.getArrivalDate().plusDays(1);
        LocalDate departureDate = booking.getDepartureDate().plusDays(1);
        UpdateBookingControllerRequest request = new UpdateBookingControllerRequest(null, null, arrivalDate, departureDate);
        String url = String.format("/booking/%s", booking.getId());
        this.testPATCHMethod(url, request, 200);
        int bookingStayCount = arrivalDate.datesUntil(departureDate).collect(Collectors.toList()).size();
        Assert.assertEquals(bookingStayCount, this.calendarAvailableRepository.getBookedDates(arrivalDate, departureDate).size());
    }

    private void testPOSTMethod(String uri, CreateBookingControllerRequest body, int expectedStatus) throws Exception {
        String requestJson = objectMapper.writeValueAsString(body);
        MvcResult mvcResult = this.mvc.perform(MockMvcRequestBuilders.post(uri).content(requestJson).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        MockHttpServletResponse httpResponse = mvcResult.getResponse();
        Assert.assertEquals(expectedStatus, httpResponse.getStatus());

    }

    private void testPATCHMethod(String uri, UpdateBookingControllerRequest body, int expectedStatus) throws Exception {
        String requestJson = objectMapper.writeValueAsString(body);
        MvcResult mvcResult = this.mvc.perform(MockMvcRequestBuilders.patch(uri).content(requestJson).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        MockHttpServletResponse httpResponse = mvcResult.getResponse();
        Assert.assertEquals(expectedStatus, httpResponse.getStatus());

    }

    private void testGETMethod(String uri, int expectedStatus) throws Exception {
        MvcResult mvcResult = this.mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        MockHttpServletResponse httpResponse = mvcResult.getResponse();
        Assert.assertEquals(expectedStatus, httpResponse.getStatus());
    }

    private void testDELETEMethod(String uri, int expectedStatus) throws Exception {
        MvcResult mvcResult = this.mvc.perform(MockMvcRequestBuilders.delete(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        MockHttpServletResponse httpResponse = mvcResult.getResponse();
        Assert.assertEquals(expectedStatus, httpResponse.getStatus());
    }

}
