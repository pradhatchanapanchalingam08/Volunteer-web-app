package com.example.ngo.dto;

import jakarta.validation.constraints.NotNull;

/**
 * signupId is required for POST (checked in the service); it is ignored for PUT.
 * The hours rule is enforced in AttendanceService, not here.
 */
public record AttendanceRequest(
        Long signupId,
        @NotNull(message = "Attended (true/false) is required") Boolean attended,
        Double hoursContributed) {
}
