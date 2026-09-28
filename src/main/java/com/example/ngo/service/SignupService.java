package com.example.ngo.service;

import com.example.ngo.dto.SignupRequest;
import com.example.ngo.dto.SignupResponse;
import com.example.ngo.entity.AttendanceRecord;
import com.example.ngo.entity.Event;
import com.example.ngo.entity.Signup;
import com.example.ngo.entity.Volunteer;
import com.example.ngo.exception.ConflictException;
import com.example.ngo.exception.ResourceNotFoundException;
import com.example.ngo.repository.EventRepository;
import com.example.ngo.repository.SignupRepository;
import com.example.ngo.repository.VolunteerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SignupService {

    private final SignupRepository signupRepository;
    private final EventRepository eventRepository;
    private final VolunteerRepository volunteerRepository;

    public SignupService(SignupRepository signupRepository,
                         EventRepository eventRepository,
                         VolunteerRepository volunteerRepository) {
        this.signupRepository = signupRepository;
        this.eventRepository = eventRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional
    public SignupResponse register(SignupRequest request) {
        // 1. Find the event (row is locked so concurrent signups cannot exceed capacity)
        Event event = eventRepository.findByIdForUpdate(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + request.eventId()));

        Volunteer volunteer = volunteerRepository.findById(request.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Volunteer not found with id " + request.volunteerId()));

        // Duplicate check
        if (signupRepository.existsByEventIdAndVolunteerId(event.getId(), volunteer.getId())) {
            throw new ConflictException("Volunteer is already registered for this event.");
        }

        // 2-5. Get capacity, count existing signups, compare
        int capacity = event.getCapacity();
        long currentCount = signupRepository.countByEventId(event.getId());

        // 6. Reject when full
        if (currentCount >= capacity) {
            throw new ConflictException("Event is full. No volunteer slots are available.");
        }

        // 7. Save only when a slot is available
        Signup signup = new Signup();
        signup.setEvent(event);
        signup.setVolunteer(volunteer);
        return toResponse(signupRepository.save(signup));
    }

    /** Both filters are optional. */
    @Transactional(readOnly = true)
    public List<SignupResponse> getAll(Long eventId, Long volunteerId) {
        List<Signup> signups;
        if (eventId != null && volunteerId != null) {
            signups = signupRepository.findByEventIdAndVolunteerId(eventId, volunteerId);
        } else if (eventId != null) {
            signups = signupRepository.findByEventId(eventId);
        } else if (volunteerId != null) {
            signups = signupRepository.findByVolunteerId(volunteerId);
        } else {
            signups = signupRepository.findAll();
        }
        return signups.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SignupResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public void delete(Long id) {
        // Attendance record (if any) is removed by JPA cascade.
        signupRepository.delete(findOrThrow(id));
    }

    private Signup findOrThrow(Long id) {
        return signupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Signup not found with id " + id));
    }

    private SignupResponse toResponse(Signup s) {
        AttendanceRecord a = s.getAttendanceRecord();
        return new SignupResponse(
                s.getId(),
                s.getEvent().getId(), s.getEvent().getName(), s.getEvent().getEventDate(), s.getEvent().getLocation(),
                s.getVolunteer().getId(), s.getVolunteer().getName(), s.getVolunteer().getEmail(),
                s.getSignupDate(),
                a == null ? null : a.getId(),
                a == null ? null : a.isAttended(),
                a == null ? null : a.getHoursContributed());
    }
}
