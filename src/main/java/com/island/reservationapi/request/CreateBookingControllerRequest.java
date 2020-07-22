package com.island.reservationapi.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.island.reservationapi.validation.ValidateDateRange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@ValidateDateRange(arrivalDate = "arrivalDate", departureDate = "departureDate")
public class CreateBookingControllerRequest {

    @NotBlank
    private final String userName;
    @Email
    @NotBlank
    private final String userEmail;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate arrivalDate;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate departureDate;


}
