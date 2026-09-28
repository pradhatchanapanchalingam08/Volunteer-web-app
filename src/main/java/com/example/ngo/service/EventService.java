package com.example.ngo.service;

import com.example.ngo.dto.EventRequest;
import com.example.ngo.dto.EventResponse;
import com.example.ngo.entity.Event;
import com.example.ngo.exception.BusinessException;
import com.example.ngo.exception.ResourceNotFoundException;
import com.example.ngo.repository.EventRepository;
import com.example.ngo.repository.SignupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final SignupRepository signupRepository;

    public EventService(EventRepository eventRepository, SignupRepository signupRepository) {
        this.eventRepository = eventRepository;
        this.signupRepository = signupRepository;
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = new Event();
        apply(event, request);
        return toResponse(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAll() {
        return eventRepository.findAllByOrderByEventDateAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = findOrThrow(id);
        long registered = signupRepository.countByEventId(id);
        if (request.capacity() < registered) {
            throw new BusinessException("Capacity cannot be less than the number of already registered volunteers ("
                    + registered + ").");
        }
        apply(event, request);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void delete(Long id) {
        // Related signups and attendance records are removed by JPA cascade.
        eventRepository.delete(findOrThrow(id));
    }

    /** Upcoming = event date is today or later; optional "until" date narrows the range. */
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcoming(LocalDate until) {
        LocalDate today = LocalDate.now();
        List<Event> events;
        if (until == null) {
            events = eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(today);
        } else if (until.isBefore(today)) {
            throw new BusinessException("The 'until' date cannot be in the past.");
        } else {
            events = eventRepository.findByEventDateBetweenOrderByEventDateAsc(today, until);
        }
        return events.stream().map(this::toResponse).toList();
    }

    private Event findOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));
    }

    private void apply(Event event, EventRequest request) {
        event.setName(request.name().trim());
        event.setDescription(request.description());
        event.setEventDate(request.date());
        event.setLocation(request.location().trim());
        event.setCapacity(request.capacity());
    }

    private EventResponse toResponse(Event e) {
        long registered = signupRepository.countByEventId(e.getId());
        long openSlots = Math.max(0L, e.getCapacity() - registered); // capacity - registered
        return new EventResponse(e.getId(), e.getName(), e.getDescription(), e.getEventDate(),
                e.getLocation(), e.getCapacity(), registered, openSlots);
    }
}
