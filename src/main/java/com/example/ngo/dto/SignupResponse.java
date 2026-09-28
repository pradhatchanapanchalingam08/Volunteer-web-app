package com.example.ngo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SignupResponse(
        Long id,
        Long eventId,
        String eventName,
        LocalDate eventDate,
        String eventLocation,
        Long volunteerId,
        String volunteerName,
        String volunteerEmail,
        LocalDateTime signupDate,
        Long attendanceId,
        Boolean attended,
        Double hoursContributed) {
}
