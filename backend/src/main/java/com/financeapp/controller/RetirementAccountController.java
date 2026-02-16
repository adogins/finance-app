package com.financeapp.controller;

import com.financeapp.dto.RetirementAccountDto;
import com.financeapp.entity.RetirementAccount;
import com.financeapp.entity.User;
import com.financeapp.repository.RetirementAccountRepository;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/retirement-accounts")
@CrossOrigin(origins = "http://localhost:3000")
public class RetirementAccountController {
    private final RetirementAccountRepository retirementAccountRepository;
    private final UserRepository userRepository;

    public RetirementAccountController(RetirementAccountRepository retirementAccountRepository, UserRepository userRepository) {
        this.retirementAccountRepository = retirementAccountRepository;
        this.userRepository = userRepository;
    }

    // GET all retirement accounts
    @GetMapping
    public List<RetirementAccountDto.Response> getAllRetirementAccounts(@PathVariable Long userId) {
        findUserOrThrow(userId);
        return retirementAccountRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET sinlge retirement account
    @GetMapping("/{id}")
    public RetirementAccountDto.Response getRetirementAccountById(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        return toResponse(findAccountOrThrow(id, userId));
    }

    // POST create retirement account
    @PostMapping
    public ResponseEntity<RetirementAccountDto.Response> createRetirementAccount(@PathVariable Long uerId, @RequestBody RetirementAccountDtop.Request request) {
        User user = findUserOrThrow(userId);

        RetirementAccount account = new RetirementAccount(
            user,
            request.getName(),
            request.getProvider(),
            request.getBalance(),
            request.getMonthlyContribution(),
            request.getEmployerMatch(),
            request.getExpectedReturnRate()
        );

        retirementAccount saved = retirementAccountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // PUT update retirement account
    @PutMapping("/{id}")
    public RetirementAccountDto.Response updateAccount(@PathVariable Long userId, @PathVariable Long id, @RequestBody RetirementAccountDto.Request request) {
        findUserOrThrow(userId);
        RetirementAccount account = findRetirementAccountOrThrow(id, userId);

        account.setName(request.getName());
        account.setProvider(request.getProvider());
        account.setBalance(request.getBalance());
        account.setMonthlyContribution(request.getMonthlyContribution());
        account.setEmployerMatch(request.getEmployerMatch());
        account.setExpectedReturnRate(request.getExpectedReturnRate());

        return toResponse(retirementAccountRepository.save(account));
    }

    // DELETE retirement account
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRetirementAccount(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        findRetirementAccountOrThrow(id, userId);
        retirementAccountRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private RetirementAccount findRetirementAccountOrThrow(Long id, Long userId) {
        RetirementAccount account = retirementAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Retirement account not found with id: " + id));
        if (!account.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Retirement account not found with id: " + id);
        }
        return account;
    }

    private RetirementAccountDto.Response toResponse(RetirementAccount account) {
        RetirementAccountDto.Response response = new RetirementAccountDto.Response();
        response.setId(account.getId());
        response.setUserId(account.getUser().getId());
        response.setName(account.getName());
        response.setProvider(account.getProvider());
        response.setBalance(account.getBalance());
        response.setMonthlyContribution(account.getMonthlyContribution());
        response.setEmployerMatch(account.getEmployerMatch());
        response.setExpectedReturnRate(account.getExpectedReturnRate());
        return response;
    }
}