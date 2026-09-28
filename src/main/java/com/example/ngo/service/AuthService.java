package com.example.ngo.service;

import com.example.ngo.dto.AuthResponse;
import com.example.ngo.dto.LoginRequest;
import com.example.ngo.dto.RegisterRequest;
import com.example.ngo.entity.Role;
import com.example.ngo.entity.User;
import com.example.ngo.entity.Volunteer;
import com.example.ngo.exception.BusinessException;
import com.example.ngo.exception.ConflictException;
import com.example.ngo.repository.UserRepository;
import com.example.ngo.repository.VolunteerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;

    public AuthService(UserRepository userRepository, VolunteerRepository volunteerRepository) {
        this.userRepository = userRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hash(request.password()));
        user.setRole(request.role());

        Volunteer volunteer = null;
        if (request.role() == Role.VOLUNTEER) {
            if (volunteerRepository.findByEmailIgnoreCase(email).isPresent()) {
                throw new ConflictException("A volunteer with this email already exists.");
            }
            volunteer = new Volunteer();
            volunteer.setName(request.name().trim());
            volunteer.setEmail(email);
            volunteer.setPhone(request.phone().trim());
            volunteer = volunteerRepository.save(volunteer);
            user.setVolunteer(volunteer);
        }

        user = userRepository.save(user);
        return toResponse(user, request.name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BusinessException("Invalid email or password."));
        if (!user.getPasswordHash().equals(hash(request.password()))) {
            throw new BusinessException("Invalid email or password.");
        }
        String name = user.getRole() == Role.VOLUNTEER && user.getVolunteer() != null
                ? user.getVolunteer().getName() : "Administrator";
        return toResponse(user, name);
    }

    private AuthResponse toResponse(User user, String name) {
        return new AuthResponse(user.getId(), name, user.getEmail(), user.getRole(),
                user.getVolunteer() == null ? null : user.getVolunteer().getId());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format("%02x", b));
            return out.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Password hashing is unavailable", e);
        }
    }
}
