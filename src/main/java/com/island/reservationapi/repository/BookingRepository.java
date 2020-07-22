package com.island.reservationapi.repository;

import com.island.reservationapi.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long> {


    @Override
    Optional<Booking> findById(Long id);

    @Query("SELECT COUNT(b.id) from Booking b where b.userName = :userName")
    int findByUserName(@Param("userName") String serName);

}
