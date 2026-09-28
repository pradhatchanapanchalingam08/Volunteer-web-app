package com.example.ngo.dto;

import java.time.LocalDateTime;

public record VolunteerResponse(
        Long id,
        String name,
        String email,
        String phone,
        LocalDateTime createdDate,
        double totalHours) {
}
