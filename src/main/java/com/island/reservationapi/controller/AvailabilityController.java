package com.island.reservationapi.controller;

import com.island.reservationapi.dto.AvailabilityDTO;
import com.island.reservationapi.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
@Validated
public class AvailabilityController {

    private final AvailabilityService availabilityService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AvailabilityDTO> getAvailableDates(
            @Future @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Future @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        from = Optional.ofNullable(from).orElse(LocalDate.now().plusDays(1));
        to = Optional.ofNullable(to).orElse(from.plusDays(30));
        List<LocalDate> availableDates = this.availabilityService.getAvailableDates(from, to);
        return ResponseEntity.status(HttpStatus.OK).body(AvailabilityDTO.builder().from(from).to(to).availableDates(availableDates).build());
    }

}
