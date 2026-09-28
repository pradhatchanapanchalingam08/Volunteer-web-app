package com.example.ngo.controller;

import com.example.ngo.dto.SignupRequest;
import com.example.ngo.dto.SignupResponse;
import com.example.ngo.service.SignupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/signups")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse register(@Valid @RequestBody SignupRequest request) {
        return signupService.register(request);
    }

    @GetMapping
    public List<SignupResponse> getAll(@RequestParam(required = false) Long eventId,
                                       @RequestParam(required = false) Long volunteerId) {
        return signupService.getAll(eventId, volunteerId);
    }

    @GetMapping("/{id}")
    public SignupResponse getById(@PathVariable Long id) {
        return signupService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        signupService.delete(id);
    }
}
