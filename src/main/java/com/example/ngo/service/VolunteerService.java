package com.example.ngo.service;

import com.example.ngo.dto.VolunteerHoursResponse;
import com.example.ngo.dto.VolunteerRequest;
import com.example.ngo.dto.VolunteerResponse;
import com.example.ngo.entity.Volunteer;
import com.example.ngo.exception.ConflictException;
import com.example.ngo.exception.ResourceNotFoundException;
import com.example.ngo.repository.AttendanceRecordRepository;
import com.example.ngo.repository.VolunteerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public VolunteerService(VolunteerRepository volunteerRepository,
                            AttendanceRecordRepository attendanceRecordRepository) {
        this.volunteerRepository = volunteerRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Transactional
    public VolunteerResponse create(VolunteerRequest request) {
        volunteerRepository.findByEmailIgnoreCase(request.email().trim()).ifPresent(v -> {
            throw new ConflictException("A volunteer with this email already exists.");
        });
        Volunteer volunteer = new Volunteer();
        apply(volunteer, request);
        return toResponse(volunteerRepository.save(volunteer));
    }

    @Transactional(readOnly = true)
    public List<VolunteerResponse> getAll() {
        return volunteerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VolunteerResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public VolunteerResponse update(Long id, VolunteerRequest request) {
        Volunteer volunteer = findOrThrow(id);
        volunteerRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("A volunteer with this email already exists.");
                });
        apply(volunteer, request);
        return toResponse(volunteerRepository.save(volunteer));
    }

    @Transactional
    public void delete(Long id) {
        // Volunteer's signups and attendance records are removed by JPA cascade.
        volunteerRepository.delete(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public VolunteerHoursResponse getTotalHours(Long id) {
        Volunteer volunteer = findOrThrow(id);
        return new VolunteerHoursResponse(volunteer.getId(), volunteer.getName(), totalHours(id));
    }

    private double totalHours(Long volunteerId) {
        Double sum = attendanceRecordRepository.sumHoursByVolunteerId(volunteerId);
        return sum == null ? 0.0 : sum;
    }

    private Volunteer findOrThrow(Long id) {
        return volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found with id " + id));
    }

    private void apply(Volunteer volunteer, VolunteerRequest request) {
        volunteer.setName(request.name().trim());
        volunteer.setEmail(request.email().trim());
        volunteer.setPhone(request.phone().trim());
    }

    private VolunteerResponse toResponse(Volunteer v) {
        return new VolunteerResponse(v.getId(), v.getName(), v.getEmail(), v.getPhone(),
                v.getCreatedDate(), totalHours(v.getId()));
    }
}
