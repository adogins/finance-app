package com.financeapp.controller;

import com.financeapp.dto.IncomeAllocationDto;
import com.financeapp.entity.IncomeAllocation;
import com.financeapp.entity.User;
import com.financeapp.repository.IncomeAllocationRepository;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{user_id}/income-allocations")
@CrossOrigin(origins = "http://localhost:5173")
public class IncomeAllocationController {
    private final IncomeAllocationRepository incomeAllocationRepository;
    private final UserRepository userRepository;

    public IncomeAllocationController(IncomeAllocationRepository incomeAllocationRepository,
            UserRepository userRepository) {
        this.incomeAllocationRepository = incomeAllocationRepository;
        this.userRepository = userRepository;
    }

    // GET all income allocations ordered by priority
    @GetMapping
    public List<IncomeAllocationDto.Response> getAllAllocations(@PathVariable("user_id") Long userId) {
        findUserOrThrow(userId);
        return incomeAllocationRepository.findByUserIdOrderByPriorityAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET single allocation
    @GetMapping("/{id}")
    public IncomeAllocationDto.Response getAllocationById(@PathVariable("user_id") Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        return toResponse(findAllocationOrThrow(id, userId));
    }

    // POST create allocation
    @PostMapping
    public ResponseEntity<IncomeAllocationDto.Response> createAllocation(@PathVariable("user_id") Long userId,
            @RequestBody IncomeAllocationDto.Request request) {
        User user = findUserOrThrow(userId);

        if (incomeAllocationRepository.existsByUserIdAndCategory(userId, request.getCategory())) {
            throw new IllegalArgumentException(
                    "An allocation for category '" + request.getCategory() + "' already exists");
        }

        IncomeAllocation allocation = new IncomeAllocation(
                user,
                request.getCategory(),
                request.getAllocationType(),
                request.getAllocationValue(),
                request.getPriority());

        IncomeAllocation saved = incomeAllocationRepository.save(allocation);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // PUT update allocation
    @PutMapping("/{id}")
    public IncomeAllocationDto.Response updateAllocation(@PathVariable("user_id") Long userId, @PathVariable Long id,
            @RequestBody IncomeAllocationDto.Request request) {
        findUserOrThrow(userId);
        IncomeAllocation allocation = findAllocationOrThrow(id, userId);

        allocation.setCategory(request.getCategory());
        allocation.setAllocationType(request.getAllocationType());
        allocation.setAllocationValue(request.getAllocationValue());
        allocation.setPriority(request.getPriority());

        return toResponse(incomeAllocationRepository.save(allocation));
    }

    // DELETE allocation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllocation(@PathVariable("user_id") Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        findAllocationOrThrow(id, userId);
        incomeAllocationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private IncomeAllocation findAllocationOrThrow(Long id, Long userId) {
        IncomeAllocation allocation = incomeAllocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Income allocation not found with id: " + id));
        if (!allocation.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Income allocation not found with id: " + id);
        }
        return allocation;
    }

    private IncomeAllocationDto.Response toResponse(IncomeAllocation allocation) {
        IncomeAllocationDto.Response response = new IncomeAllocationDto.Response();
        response.setId(allocation.getId());
        response.setUserId(allocation.getUser().getId());
        response.setCategory(allocation.getCategory());
        response.setAllocationType(allocation.getAllocationType());
        response.setAllocationValue(allocation.getAllocationValue());
        response.setPriority(allocation.getPriority());
        response.setCreatedAt(allocation.getCreatedAt());
        return response;
    }
}