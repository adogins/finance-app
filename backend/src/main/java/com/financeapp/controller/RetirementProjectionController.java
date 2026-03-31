package com.financeapp.controller;

import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.RetirementProjectionService;
import com.financeapp.service.RetirementProjectionService.ProjectionSummary;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/retirement-projection")
@CrossOrigin(origins = "http://localhost:5173")
public class RetirementProjectionController {
    private final RetirementProjectionService retirementProjectionService;
    private final UserRepository userRepository;

    public RetirementProjectionController(RetirementProjectionService retirementProjectionService,
            UserRepository userRepository) {
        this.retirementProjectionService = retirementProjectionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ProjectionSummary getProjection(@PathVariable Long userId,
            @RequestParam(required = false) Integer retirementAge) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return retirementProjectionService.projectAll(user, retirementAge);
    }
}