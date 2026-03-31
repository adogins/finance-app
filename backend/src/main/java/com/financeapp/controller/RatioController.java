package com.financeapp.controller;

import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.FinanceRatioService;
import com.financeapp.service.FinanceRatioService.RatioResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/ratios")
@CrossOrigin(origins = "http://localhost:5173")
public class RatioController {
    public final FinanceRatioService financeRatioService;
    public final UserRepository userRepository;

    public RatioController(FinanceRatioService financeRatioService, UserRepository userRepository) {
        this.financeRatioService = financeRatioService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Map<String, RatioResult> getRatios(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return financeRatioService.calculateAll(user);
    }
}