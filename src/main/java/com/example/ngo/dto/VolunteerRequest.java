package com.example.ngo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VolunteerRequest(
        @NotBlank(message = "Name cannot be blank") String name,
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid") String email,
        @NotBlank(message = "Phone cannot be blank") String phone) {
}
