package com.financeapp.controller;

import com.financeapp.repository.UserRepository;
import com.financeapp.service.BalanceSheetService;
import com.financeapp.service.BalanceSheetService.MonthlyBalanceSheet;
import com.financeapp.service.BalanceSheetService.YearlyBalanceSheet;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/{userId}/balance-sheet")
@CrossOrigin(origins = "http://localhost:5173")
public class BalanceSheetController {
    private final BalanceSheetService balanceSheetService;
    private final UserRepository userRepository;

    public BalanceSheetController(BalanceSheetService balanceSheetService, UserRepository userRepository) {
        this.balanceSheetService = balanceSheetService;
        this.userRepository = userRepository;
    }

    // Monthly
    @GetMapping("/monthly")
    public MonthlyBalanceSheet getMonthly(@PathVariable Long userId, @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        findUserOrThrow(userId);
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();

        return balanceSheetService.getMonthly(userId, y, m);
    }

    // Yearly
    @GetMapping("/yearly")
    public YearlyBalanceSheet getYearly(@PathVariable Long userId, @RequestParam(required = false) Integer year) {
        findUserOrThrow(userId);
        int y = year != null ? year : LocalDate.now().getYear();

        return balanceSheetService.getYearly(userId, y);
    }

    // Helpers
    private void findUserOrThrow(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }
}