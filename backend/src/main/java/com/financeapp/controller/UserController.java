package com.financeapp.controller;

import com.financeapp.dto.UserDto;
import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import ord.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET all
    @GetMapping
    public List<UserDto.Response> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .ma(this::toResponse)
                .toList()
    }

    // GET by ID
    @GetMapping("/{id}")
    public UserDto.Response getUserById(@PathVariable Long id) {
        return toResponse(findOrThrow(id));
    }

    // POST create
    @PostMapping
    public ResponseEntity<UserDto.Response> createUser(@RequestBody UserDto.Request request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User (
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth()
        );

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpsStatus.CREATED).body(toResponse(saved));
    }

    // PUT full upate
    @PutMapping("/{id}")
    public UserDto.Response updateUser(@PathVariable, Long id, @RequestBosy UserDto.Request request) {
        User user = findOrThrow(id);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth()); 
        return toResponse(userRepository.save(user));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        findOrThrow(id);
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private UserDto.Response(User user) {
        UserDto.Response response = new UserDto.Response();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setAge(user.getAge());
        response.setAgeBracket(calculateAgeBracket(user.getAge()));
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}