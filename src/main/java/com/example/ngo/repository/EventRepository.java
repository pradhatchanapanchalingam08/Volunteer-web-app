package com.example.ngo.repository;

import com.example.ngo.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByEventDateAsc();

    // upcoming = today or later
    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);

    List<Event> findByEventDateBetweenOrderByEventDateAsc(LocalDate from, LocalDate to);

    long countByEventDateGreaterThanEqual(LocalDate date);

    // Locks the event row so two simultaneous signups cannot both take the last slot.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
