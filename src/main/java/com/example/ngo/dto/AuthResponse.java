package com.example.ngo.dto;

import com.example.ngo.entity.Role;

public record AuthResponse(Long userId, String name, String email, Role role, Long volunteerId) {
}
