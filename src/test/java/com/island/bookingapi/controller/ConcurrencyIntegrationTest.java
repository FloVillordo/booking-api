package com.island.bookingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.island.bookingapi.repository.BookingRepository;
import com.island.bookingapi.request.CreateBookingControllerRequest;
import database.BookingPostgresqlContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConcurrencyIntegrationTest {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = BookingPostgresqlContainer.getInstance();

    private MockMvc mvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;
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
    public void testSameBookingConcurrency() throws InterruptedException {
        Runnable creationBooking = this.getRunnable("Fercho Recalt", "pepe@gmail.com", LocalDate.now().plusDays(10), LocalDate.now().plusDays(12));
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            threadList.add(new Thread(creationBooking));
        }
        this.runThreads(threadList);
        Assert.assertEquals(this.bookingRepository.findByUserName("Fercho Recalt"), 1);

    }

    @Test
    @Transactional
    public void testSeveralReservationConcurrency() throws InterruptedException {
        int bookingBeforeTest = this.bookingRepository.findAll().size();
        Runnable creationBookingUser1 = this.getRunnable("Pepe Pepito", "pepe@gmail.com", LocalDate.now().plusDays(15), LocalDate.now().plusDays(16));
        Runnable creationBookingUser2 = this.getRunnable("Juan PEPE", "juanpepe@gmail.com", LocalDate.now().plusDays(16), LocalDate.now().plusDays(18));
        Runnable creationBookingUser3 = this.getRunnable("Zac Efron", "zacefrom@gmail.com", LocalDate.now().plusDays(20), LocalDate.now().plusDays(21));
        Runnable creationBookingUser4 = this.getRunnable("Gabriela Montes", "gabymontes@gmail.com", LocalDate.now().plusDays(24), LocalDate.now().plusDays(26));
        List<Thread> threadList = new ArrayList<>();
        threadList.add(new Thread(creationBookingUser1));
        threadList.add(new Thread(creationBookingUser2));
        threadList.add(new Thread(creationBookingUser3));
        threadList.add(new Thread(creationBookingUser4));
        this.runThreads(threadList);
        Assert.assertEquals(this.bookingRepository.findAll().size(), bookingBeforeTest + 4);

    }

    private void runThreads(List<Thread> threadList) throws InterruptedException {
        for (Thread t : threadList) {
            t.start();
        }
        for (Thread t : threadList) {
            t.join();
        }
    }

    @Test
    @Transactional
    public void testSameBookingCreationConcurrency() throws InterruptedException {
        int bookingBeforeTest = this.bookingRepository.findAll().size();
        int numberOfThreads = 100;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    this.getBooking("Pepe Pepito", "pepe@gmail.com", LocalDate.now().plusDays(27), LocalDate.now().plusDays(28));
                } catch (Exception e) {
                }
                latch.countDown();
            });
        }
        latch.await();
        Assert.assertEquals(this.bookingRepository.findAll().size(), bookingBeforeTest + 1);
    }


    private Runnable getRunnable(String fullName, String email, LocalDate from, LocalDate to) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    ConcurrencyIntegrationTest.this.getBooking(fullName, email, from, to);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void getBooking(String fullName, String email, LocalDate from, LocalDate to) throws Exception {
        CreateBookingControllerRequest request = new CreateBookingControllerRequest(fullName, email, from, to);
        String requestJson = objectMapper.writeValueAsString(request);
        MvcResult mvcResult = ConcurrencyIntegrationTest.this.mvc.perform(MockMvcRequestBuilders.post("/booking").content(requestJson).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        mvcResult.getResponse();
    }

}
