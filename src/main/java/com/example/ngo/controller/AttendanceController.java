package com.example.ngo.controller;

import com.example.ngo.dto.AttendanceRequest;
import com.example.ngo.dto.AttendanceResponse;
import com.example.ngo.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse mark(@Valid @RequestBody AttendanceRequest request) {
        return attendanceService.mark(request);
    }

    @GetMapping
    public List<AttendanceResponse> getAll() {
        return attendanceService.getAll();
    }

    @GetMapping("/{id}")
    public AttendanceResponse getById(@PathVariable Long id) {
        return attendanceService.getById(id);
    }

    @GetMapping("/signup/{signupId}")
    public AttendanceResponse getBySignup(@PathVariable Long signupId) {
        return attendanceService.getBySignupId(signupId);
    }

    @PutMapping("/{id}")
    public AttendanceResponse update(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return attendanceService.update(id, request);
    }
}
