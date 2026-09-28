package com.example.ngo.controller;

import com.example.ngo.dto.VolunteerHoursResponse;
import com.example.ngo.dto.VolunteerRequest;
import com.example.ngo.dto.VolunteerResponse;
import com.example.ngo.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VolunteerResponse create(@Valid @RequestBody VolunteerRequest request) {
        return volunteerService.create(request);
    }

    @GetMapping
    public List<VolunteerResponse> getAll() {
        return volunteerService.getAll();
    }

    @GetMapping("/{id}")
    public VolunteerResponse getById(@PathVariable Long id) {
        return volunteerService.getById(id);
    }

    @GetMapping("/{id}/hours")
    public VolunteerHoursResponse getTotalHours(@PathVariable Long id) {
        return volunteerService.getTotalHours(id);
    }

    @PutMapping("/{id}")
    public VolunteerResponse update(@PathVariable Long id, @Valid @RequestBody VolunteerRequest request) {
        return volunteerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        volunteerService.delete(id);
    }
}
