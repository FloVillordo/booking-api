package com.island.bookingapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String userName;
    @NotBlank
    private String userEmail;

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Integer status;

    public Booking(String userName, String userEmail, LocalDate arrivalDate, LocalDate departureDate) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.status = 0;
    }
}
