package com.example.ngo.dto;

import jakarta.validation.constraints.NotNull;

public record SignupRequest(
        @NotNull(message = "Event ID is required") Long eventId,
        @NotNull(message = "Volunteer ID is required") Long volunteerId) {
}
