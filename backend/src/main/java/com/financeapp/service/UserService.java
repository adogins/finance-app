package com.financeapp.service;

import com.financeapp.dto.UserDto;
import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto.Response> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserDto.Response getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public UserDto.Response createUser(UserDto.Request request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User user = new User(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                passwordEncoder.encode(request.getPassword())
        );

        return toResponse(userRepository.save(user));
    }

    public UserDto.Response login(UserDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return toResponse(user);
    }

    public UserDto.Response updateUser(Long id, UserDto.Request request) {
        User user = findOrThrow(id);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        findOrThrow(id);
        userRepository.deleteById(id);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private UserDto.Response toResponse(User user) {
        UserDto.Response response = new UserDto.Response();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setAge(user.getAge());
        response.setAgeBracket(user.getAgeBracket());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}