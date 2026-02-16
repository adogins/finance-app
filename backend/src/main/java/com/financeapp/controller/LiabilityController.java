package com.financeapp.controller;

import com.financeapp.dto.LiabilityDto;
import com.financeapp.entity.Liability;
import com.financeapp.entity.User;
import com.financeapp.repository.LiabilityRepository;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/liabilities")
@CrossOrigin(origins = "http://localhost:3000")
public class LiabilityController {
    private final LiabilityRepository liabilityRepository;
    private final UserRepository userRepository;

    public LiabilityController(LiabilityRepository liabilityRepository, UserRepository userRepository) {
        this.liabilityRepository = liabilityRepository;
        this.userRepository = userRepository;
    }

    // Get all liabilities
    @GetMapping
    public List<LiabilityDto.Response> getAllLiabilities(@PathVariable Long userId, @RequestParam(required = false) String type) {
        findUserOrThrow(userId);
        if (type != null && !type.isBlank()) {
            return liabilityRepository.findByUserIdAndType(userId, type)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return liabilityRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET single liability
    @GetMapping("/{id}")
    public LiabilityDto.Response getLiabilityById(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        return toResponse(findLiabilityOrThrow(id, userId));
    }

    // POST create liability
    @PostMapping
    public ResponseEntity<LiabilityDto.Response> createLiability(@PathVariable Long userId, @RequestBody LiabilityDto.Request request) {
        User user = findUserOrThrow(userId);
        Liability liability = new Liability(
            user,
            request.getName(),
            request.getType(),
            request.getBalance(),
            request.getInterestRate()
        );

        Liability saved = liabilityRepository.save(liability);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // PUT update liability
    @PutMapping("/{id}")
    public LiabilityDto.Response updateLiability(@PathVariable Long userId, @PathVariable Long id, @RequestBody LiabilityDto.Request request) {
        findUserOrThrow(userId);
        Liability liability = findLiabilityOrThrow(id, userId);

        liability.setName(request.getName());
        liability.setType(request.getType());
        liability.setBalance(request.getBalance());
        liability.setInterestRate(request.getInterestRate());

        return toResponse(liabilityRepository.save(liability));
    }

    // DELETE liability
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiability(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        findLiabilityOrThrow(id, userId);
        liabilityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private Liability findLiabilityOrThrow(Long id, Long userId) {
        Liability liability = liabilityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Liability not found with id: " + id));
        if (!liability.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Liability not found with id: " + id);
        }
        return liability;
    }

    private LiabilityDto.Response toResponse(Liability liability) {
        LiabilityDto.Response response = new LiabilityDto.Response();
        response.setId(liability.getId());
        response.setUserId(liability.getUser().getId());
        response.setName(liability.getName());
        response.setType(liability.getType());
        response.setBalance(liability.getBalance());
        response.setInterestRate(liability.getInterestRate());
        response.setUpdatedAt(liability.getUpdatedAt());
        return response;
    }
}