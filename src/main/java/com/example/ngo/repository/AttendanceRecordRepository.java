package com.example.ngo.repository;

import com.example.ngo.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findBySignupId(Long signupId);

    boolean existsBySignupId(Long signupId);

    // Only records where the volunteer attended are counted.
    @Query("select coalesce(sum(a.hoursContributed), 0.0) from AttendanceRecord a "
            + "where a.signup.volunteer.id = :volunteerId and a.attended = true")
    Double sumHoursByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("select coalesce(sum(a.hoursContributed), 0.0) from AttendanceRecord a where a.attended = true")
    Double sumAllAttendedHours();
}
