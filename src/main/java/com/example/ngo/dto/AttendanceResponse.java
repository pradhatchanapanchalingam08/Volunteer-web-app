package com.example.ngo.dto;

import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long signupId,
        Long eventId,
        String eventName,
        Long volunteerId,
        String volunteerName,
        boolean attended,
        double hoursContributed,
        LocalDateTime markedDate) {
}
