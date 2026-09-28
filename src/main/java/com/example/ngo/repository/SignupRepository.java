package com.example.ngo.repository;

import com.example.ngo.entity.Signup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignupRepository extends JpaRepository<Signup, Long> {

    long countByEventId(Long eventId);

    boolean existsByEventIdAndVolunteerId(Long eventId, Long volunteerId);

    List<Signup> findByEventId(Long eventId);

    List<Signup> findByVolunteerId(Long volunteerId);

    List<Signup> findByEventIdAndVolunteerId(Long eventId, Long volunteerId);
}
