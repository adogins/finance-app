package com.financeapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.dto.UserDTO;
import com.financeapp.entitty.User;
import com.financeapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Usercontroller
 * 
 * Uses @WebMvcTest to test the controller layer MockMvc.
 * Repository is mocked - no database required.
 * 
 * Test all 5 endpoints:
 *  GET  /api/users           -> list all
 *  GET  /api/users/{id}      -> get by id
 *  POST /api/users           -> create
 *  PUT  /api/users/{id}      -> update
 *  DELETE /api/users/{id}    -> delete
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private UserDto.Request testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "John", "Doe", localDate.of(1990, 5, 15));
    
        // Simulate saved entity with ID
        testUser.setId(1L);

        testRequest = new UserDto.Request();
        testRequest.setEmail("test#example.com");
        testRequest.setFirstName("John");
        testRequest.setLastName("Doe");
        testRequest.setDateOfBirth(LocalDate.of(1990, 5, 15));
    }

    // GET /api/users
    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {
        @Test
        @DisplayName("Returns 200 with list of users")
        void getAllUsers_returns_200_wit_list() throws Exception {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[0].age").exists())
                .andExpect(jsonPath("$[0].ageBracket").exists());
        }

        @Test
        @DisplayName("Returns empty array when no users exist")
        void getAllUsers_returns_empty_array() throws Exception {
            when(userRepository.findAll()).thenReturn(List.of());

            mocMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // GET /api/users/{id}
    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {
        @Test
        @DisplayName("Returns 200 with user when found")
        void getUserById_returns_200_when_found() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
        }

        @Test
        @DisplayName("Returns 404 when user not found")
        void getUserById_returns_404_when_not_found() throws Exception {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
        }
    }

    // POST /api/users
    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {
        @Test
        @DisplayName("Returns 201 with created user")
        void createUser_returns_201() throws Exception {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.email").value("test@example.com"));

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Returns 400 when email already exists")
        void createUser_returns_400_when_email_exists() throws Exception {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value(containsString("Email already registered")));

            verify(userRepository, never()).save(any());
        }
    }

    // PUT /api/users/{id}
    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {
        @Test
        @DisplayName("Returns 200 with updated user")
        void updateUser_returns_200() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDto.Request updateRequest = new UserDto.Request();
            updateRequest.setEmail("updated@example.com");
            updateRequest.setFirstName("Jane");
            updateRequest.setLastName("Smith");
            updateRequest.setDateOfBirth(LocalDate.of(1992, 4, 12));

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1));
                    
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Returns 404 when user not found")
        void updateUser_returns_404_when_not_found() throws Exception {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                        .andExpect(status().isNotFound())
        
            verify(userRepository, never()).save(any());
        }
    }

    // DELETE /api/users/{id}
    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {
        @Test
        @DisplayName("Returns 204 when user deleted")
        void deleteUser_returns_204() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).deleteById(1L);

            mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Returns 404 when user not found")
        void deleteUser_returns_404_when_not_found() throws Exception {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound())

            verify(userRepository, never()).deleteById(any());
        }
    }
}