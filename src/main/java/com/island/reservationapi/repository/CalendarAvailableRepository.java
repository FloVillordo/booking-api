package com.island.reservationapi.repository;

import com.island.reservationapi.model.CalendarAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarAvailableRepository
        extends JpaRepository<CalendarAvailability, Long> {

    @Query("SELECT c.day from CalendarAvailability c where c.day between :initialDate and :endDate order by c.day asc")
    List<LocalDate> getOrderedBookedDates(@Param("initialDate") LocalDate initialDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT c.day from CalendarAvailability c where c.day between :initialDate and :endDate")
    List<LocalDate> getBookedDates(@Param("initialDate") LocalDate initialDate, @Param("endDate") LocalDate endDate);


    @Modifying
    @Query("DELETE from CalendarAvailability c where c.day in :bookingDates")
    void deleteByDates(@Param("bookingDates") List<LocalDate> bookingDates);
}
