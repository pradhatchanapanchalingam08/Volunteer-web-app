package com.example.ngo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record EventRequest(
        @NotBlank(message = "Event name cannot be blank") String name,
        String description,
        @NotNull(message = "Event date cannot be null") LocalDate date,
        @NotBlank(message = "Location cannot be blank") String location,
        @NotNull(message = "Volunteer capacity is required")
        @Positive(message = "Volunteer capacity must be positive") Integer capacity) {
}
