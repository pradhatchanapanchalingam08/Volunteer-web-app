package com.example.ngo.dto;

import java.time.LocalDate;

public record EventResponse(
        Long id,
        String name,
        String description,
        LocalDate date,
        String location,
        Integer capacity,
        long registeredCount,
        long openSlots) {
}
