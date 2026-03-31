package com.financeapp.controller;

import com.financeapp.dto.UserDto;
import com.financeapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto.Response> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto.Response getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public ResponseEntity<UserDto.Response> createUser(@RequestBody UserDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.Response> login(@RequestBody UserDto.LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PutMapping("/{id}")
    public UserDto.Response updateUser(@PathVariable Long id, @RequestBody UserDto.Request request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}