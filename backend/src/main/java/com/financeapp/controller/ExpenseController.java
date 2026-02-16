package com.financeapp.controller;

import com.financeapp.dto.ExpenseDto;
import com.financeapp.entity.Expense;
import com.financeapp.repository.ExpenseRepository;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/expenses")
@CrossOrigin(origins = "http://localhost:3000")
public class ExpenseController {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseController(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // GET all expenses
    @GetMapping
    public List<ExpenseDto.Response> getAllExpense(@PathVariable Long userId, @RequestParam(required = false) String category) {
        findUserOrThrow(userId);

        if (category != null && !category.isBlank()) {
            return expenseRepository.findByUserIdAndCategory(userId, category)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return expenseRepository.findByUserIdOrderBySpentAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET single expense
    @GetMapping("/{id}")
    public ExpenseDto.Response getExpenseByID(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        return toResponse(findExpenseOrThrow(id, userId));
    }

    // POST create expense
    @PostMapping
    public ResponseEntity<ExpenseDto.Response> createExpense(@PathVariable Long userId, @RequestBody ExpenseDto.Request request) {
        User user = findUserOrThrow(userId);

        Expense expense = new Expense(
                user,
                request.getAmount(),
                request.getCategory(),
                request.getDescription(),
                request.getSpentAt()
        );

        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // PUT update expense
    @PutMapping("/{id}")
    public ExpenseDto.Response updateExpense(@PathVariable Long userId                                          
                                             @PathVariable Long id,
                                             @RequestBody ExpenseDto.Request request) {
    findUserOrThrow(userId);
    Expense expense = findExpenseOrThrow(id, userId);

    expsense.setAmount(request.getAmount());
    expense.setCategory(request.getCategory());
    expense.setDescription(request.getDescription());
    expense.setSpentAt(request.getSpentAt());

    return toResponse(expenseRepository.save(expense));
    }

    // DELETE expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        findExpenseOrThrow(id, userId);
        expenseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private Expense findExpenseOrThrow(Long id, Long userId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id: " + id));
        if (!expense.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Expense not found with id: " + id);
        }
        return expense;
    }

    private ExpenseDto.Response toResponse(Expense expense) {
        ExpenseDto.Response response = new ExpenseDto.Response();
        response.setId(expense.getId());
        response.setUserId(expense.getUser().getId());
        response.setAmount(expense.getAmount());
        response.setCategory(expense.getCategory());
        response.setDescription(expense.getDescription());
        response.setSpentAt(expense.getSpentAt());
        response.setCreatedAt(expense.getCreatedAt());
        return response;
    }
}