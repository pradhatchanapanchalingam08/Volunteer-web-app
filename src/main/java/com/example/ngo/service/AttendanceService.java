package com.example.ngo.service;

import com.example.ngo.dto.AttendanceRequest;
import com.example.ngo.dto.AttendanceResponse;
import com.example.ngo.entity.AttendanceRecord;
import com.example.ngo.entity.Signup;
import com.example.ngo.exception.BusinessException;
import com.example.ngo.exception.ConflictException;
import com.example.ngo.exception.ResourceNotFoundException;
import com.example.ngo.repository.AttendanceRecordRepository;
import com.example.ngo.repository.SignupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SignupRepository signupRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
                             SignupRepository signupRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.signupRepository = signupRepository;
    }

    @Transactional
    public AttendanceResponse mark(AttendanceRequest request) {
        if (request.signupId() == null) {
            throw new BusinessException("Signup ID is required.");
        }
        Signup signup = signupRepository.findById(request.signupId())
                .orElseThrow(() -> new ResourceNotFoundException("Signup not found with id " + request.signupId()));

        if (attendanceRecordRepository.existsBySignupId(signup.getId())) {
            throw new ConflictException(
                    "Attendance is already recorded for this signup. Use PUT /api/attendance/{id} to update it.");
        }

        double hours = validateHours(request.attended(), request.hoursContributed());

        AttendanceRecord record = new AttendanceRecord();
        record.setSignup(signup);
        record.setAttended(request.attended());
        record.setHoursContributed(hours);
        record.setMarkedDate(LocalDateTime.now());
        return toResponse(attendanceRecordRepository.save(record));
    }

    @Transactional
    public AttendanceResponse update(Long id, AttendanceRequest request) {
        AttendanceRecord record = findOrThrow(id);
        double hours = validateHours(request.attended(), request.hoursContributed());
        record.setAttended(request.attended());
        record.setHoursContributed(hours);
        record.setMarkedDate(LocalDateTime.now());
        return toResponse(attendanceRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getBySignupId(Long signupId) {
        return attendanceRecordRepository.findBySignupId(signupId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No attendance record found for signup id " + signupId));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAll() {
        return attendanceRecordRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * CORE BUSINESS RULE:
     *  - not attended  -> hours must be empty or 0 (hours can only be logged for attended volunteers)
     *  - attended      -> hours must be greater than 0 (no zero, negative or invalid values)
     */
    private double validateHours(boolean attended, Double hours) {
        if (!attended) {
            if (hours != null && hours != 0) {
                throw new BusinessException(
                        "Hours contributed can only be logged for a volunteer marked as attended.");
            }
            return 0.0;
        }
        if (hours == null || hours.isNaN() || hours.isInfinite() || hours <= 0) {
            throw new BusinessException(
                    "Hours contributed must be greater than 0 for a volunteer marked as attended.");
        }
        return hours;
    }

    private AttendanceRecord findOrThrow(Long id) {
        return attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id " + id));
    }

    private AttendanceResponse toResponse(AttendanceRecord a) {
        Signup s = a.getSignup();
        return new AttendanceResponse(a.getId(), s.getId(),
                s.getEvent().getId(), s.getEvent().getName(),
                s.getVolunteer().getId(), s.getVolunteer().getName(),
                a.isAttended(), a.getHoursContributed(), a.getMarkedDate());
    }
}
