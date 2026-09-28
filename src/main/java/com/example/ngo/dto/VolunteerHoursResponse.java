package com.example.ngo.dto;

public record VolunteerHoursResponse(
        Long volunteerId,
        String volunteerName,
        double totalHours) {
}
