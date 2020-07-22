package com.island.reservationapi.controller;

import com.island.reservationapi.dto.BookingDTO;
import com.island.reservationapi.model.Booking;
import com.island.reservationapi.request.CreateBookingControllerRequest;
import com.island.reservationapi.request.UpdateBookingControllerRequest;
import com.island.reservationapi.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@Validated
public class BookingController {


    private final BookingService bookingService;


    @GetMapping(value = "/{id}")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable @NotNull Long id) {
        Booking booking = this.bookingService.getBookingById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.transformResponse(booking));
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<BookingDTO> create(@RequestBody @Valid CreateBookingControllerRequest request) {
        Booking booking = this.bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingDTO.builder().bookingId(booking.getId()).build());
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingDTO> update(@RequestBody @Valid UpdateBookingControllerRequest request, @PathVariable Long id) {
        Booking booking = this.bookingService.updateBooking(request, id);
        return ResponseEntity.status(HttpStatus.OK).body(this.transformResponse(booking));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        Booking booking = this.bookingService.cancelBooking(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.transformResponse(booking));
    }

    private BookingDTO transformResponse(Booking booking) {
        return BookingDTO.builder().bookingId(booking.getId()).userName(booking.getUserName()).userEmail(booking.getUserEmail())
                .arrivalDate(booking.getArrivalDate()).departureDate(booking.getDepartureDate()).createdAt(booking.getCreatedAt()).updatedAt(booking.getUpdatedAt()).build();
    }

}
