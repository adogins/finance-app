package com.financeapp.controller;

import com.financeapp.dto.IncomeDto;
import com.financeapp.entity.Income;
import com.financeapp.entity.User;
import com.financeapp.repository.IncomeRepository;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/income")
@CrossOrigin(origins = "http://localhost:3000")
public class IncomeController {
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public IncomeController(IncomeRepository incomeRepository, UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    // GET all income for user
    @GetMapping
    public List<IncomeDto.Response> getAllIncome(@PathVariable Long userId) {
        findUserOrThrow(userId);
        return incomeRepository.findByUserIdOrderByReceivedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET single income entry
    @GetMapping("/{id}")
    public IncomeDto.Response getIncomeById(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        return toResponse(findIncomeOrThrow(id, userId));
    }

    // POST create income entry
    @PostMapping
    public ResponseEntity<IncomeDto.Response> createIncome(@PathVariable Long userId,
            @RequestBody IncomeDto.Request request) {
        User user = findUserOrThrow(userId);

        Income income = new Income(
                user,
                request.getAmount(),
                request.getSource(),
                request.getReceivedAt());

        Income saved = incomeRepository.save(income);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // PUT update income entry
    @PutMapping("/{id}")
    public IncomeDto.Response updateIncome(@PathVariable Long userId, @PathVariable Long id,
            @RequestBody IncomeDto.Request request) {
        findUserOrThrow(userId);
        Income income = findIncomeOrThrow(id, userId);

        income.setAmount(request.getAmount());
        income.setSource(request.getSource());
        income.setReceivedAt(request.getReceivedAt());

        return toResponse(incomeRepository.save(income));
    }

    // DELETE income entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        findIncomeOrThrow(id, userId);
        incomeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private Income findIncomeOrThrow(Long id, Long userId) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Income entry not found with id: " + id));
        if (!income.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Income entry not found for user id: " + userId);
        }
        return income;
    }

    private IncomeDto.Response toResponse(Income income) {
        IncomeDto.Response response = new IncomeDto.Response();
        response.setId(income.getId());
        response.setUserId(income.getUser().getId());
        response.setAmount(income.getAmount());
        response.setSource(income.getSource());
        response.setReceivedAt(income.getReceivedAt());
        response.setCreatedAt(income.getCreatedAt());
        return response;
    }
}