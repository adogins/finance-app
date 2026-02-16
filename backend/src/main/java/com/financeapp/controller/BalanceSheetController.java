package com.financeapp.controller;

import com.financeapp.repository.UserRepository;
import com.financeapp.service.BalanceSheetService;
import com.financeapp.service.BalanceSheetService.MonthlyBalance;
import com.financeapp.service.BalanceSheetService.YearlyBalance;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/{userId}/balance-sheet")
@CrossOrigin(origins = "http://localhost:3000")
public class BalanceSheetController {
    private final BalanceSheetService balanceSheetService;
    private final UserRepository userRepository;

    public BalanceSheetController(BalanceSheetService balanceSheetService, UserRepository userRepository) {
        this.balanceSheetService = balanceSheetService;
        this.userRepository = userRepository;
    }

    // Monthly
    @GetMapping("/monthly")
    public MonthlyBalanceSheet getMonthly(@PathVariable Long userId, @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {
        findUserOrThrow(userId);
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();

        return balanceSheetService.getMonthlyBalanceSheet(userId, y, m);
    }

    // Yearly
    @GetMapping("/yearly")
    public YearlyBalanceSheet getYearly(@PathVariable Long userId, @RequestParam(required = false) INteger year) {
        findUserOrThrow(userId);
        int y = year != null ? year : LocalDate.now().getYear();

        return balanceSheetService.getYearlyBalanceSheet(userId, y);
    }


    // Helpers
    private void findUserOrThrow(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }
}