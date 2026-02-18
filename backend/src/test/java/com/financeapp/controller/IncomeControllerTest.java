package com.financeapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.dto.IncomeDto;
import com.financeapp.entity.Income;
import com.financeapp.repository.IncomeRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrst.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import stattic org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for IncomeController
 * 
 * Tests scoped user access - ensures one user cannot access another user's income.
 */
@WebMvcTest(IncomeController.class)
class IncomeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IncomeRepository incomeRepository;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private Income testIncome;
    private IncomeDto.Request testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "John", "Doe", LocalDate.of(1990, 5, 15));
        testUser.setId(1L);

        testIncome = new Income(testUser, new BigDecimal("5000.00"), "Salary", LocalDate.of(2025, 3, 1));
        testIncome.setId(10L);

        testRequest = new IncomeDto.Request();
        testRequest.setAmount(new BigDecimal("5000.00"));
        testRequest.setSource("Salary");
        testRequest.setDate(LocalDate.of(2025, 3, 1));
    }

    // GET /api/users/{userId}/income
    @Nested
    @DisplayName("GET /api/users/{userId}/income")
    class GetAllIncome {
        @Test
        @DisplayName("Returns 200 with lsit of income entries for user")
        void getAllIncome_returns_200() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.findByUserIdOrderByReceivedAtDesc(1L)).thenReturn(List.of(testIncome));

            mockMvc.perform(get("/api/users/1/income"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(10))
                    .andExpect(jsonPath("$[0].userId").value(1))
                    .andExpect(jsonPath("$[0].amount").value(5000.00))
                    .andExpect(jsonPath("$[0].source").value("Salary"))
        }

        @Test
        @DisplayName("Returns 404 when user not found")
        void getAllIncome_returns_404_when_user_not_found() throws Exception {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/999/income"))
                    .andExpect(status().isNotFound());
        }
    }

    // GET /api/users/{userId}/income/{id}
    @Nested
    @DisplayName("GET /api/users/{userId}/income/{id}")
    class GetIncomeById {
        
        @Test
        @DisplayName("Returns 200 with income entry")
        void getIncomeById_returns_200() throws Exception {
            when(userRepository.findyId(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.findById(10L)).thenReturn(Optional.of(testIncome));

            mockMvc.perform(get("/api/users/1/income/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.amount").value(5000.00));
        }

        @Test
        @DisplayName("Returns 404 when income belongs to different user")
        void getIncomeById_returns_404_when_wrong_user() throws Exception {
            User otherUser = new User("other@example.com", "Jane", "Smith", LocalDate.of(1985, 1, 1));
            otherUser.setId(2L);

            Income otherIncome = new Income(otherUser, new BigDecimal("3000"), "Freelance", LocalDate.now());
            otherIncome.setId(10L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.findById(20L)).thenReturn(Optional.of(otherIncome));

            mockMvc.perform(get("/api/users/1/income/20"))
                    .andExpect(status().isNotFound());
        }
    }

    // POST /api/users/{userId}/income
    @Nested
    @DisplayName("POST /api/users/{userId}/income")
    class CreateIncome {
        @Test 
        @DisplayName("Returns 201 with created income entry")
        void createIncome_returns_201() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.save(any(Income.class))).thenReturn(testIncome);

            mockMvc.perform(post("/api/users/1/income")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.amount").value(5000.00));

            verify(incomeRepository).save(any(Income.class));
        }

        @Test
        @DisplayName("Returns 404 when user not found")
        void createIncome_returns_404_when_user_not_found() throws Exception {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/users/999/income")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isNotFound());

            verify(incomeRepository, never()).save(any());
        }
    }

    // PUT /api/users/{userId}/income/{id}
    @Nested
    @DisplayName("PUT /api/users/{userId}/income/{id}")
    class UpdateIncome {
        @Test
        @DisplayName("Returns 200 with updated income entry")
        void updateIncome_returns_200() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.findById(10L)).thenReturn(Optional.of(testIncome));
            when(incomeRepository.save(any(Income.class))).thenReturn(testIncome);

            IncomeDto.Request updateRequest = new IncomeDto.Request();
            updateRequest.setAmount(new BigDecimal("6000.00"));
            updateRequest.setSource("Salary + Bonus");
            updateRequest.setReceivedAt(LocalDate.of(2025, 3, 15));

            mockMvc.perform(put("/api/users/1/income/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            verify(incomeRepository).save(any(Income.class));
        }

        @Test
        @DisplayName("Returns 404 when income belongs to different user")
        void updateIncome_returns_404_when_wrong_user() throws Exception {
            User otherUser = new User("other@example.com", "Jane", "Smith", LocalDate.of(1985, 1, 1));
            otherUser.setId(2L);

            Income otherIncome = new Income(otherUser, new BigDecimal("3000"), "Freelance", LocalDate.now());
            otherIncome.setId(20L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.findById(20L)).thenReturn(Optional.of(otherIncome));
            mockMvc.perform(put("/api/users/1/income/20")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isNotFound());

            verify(incomeRepository, never()).save(any());
        }
    }

    // DELETE /api/users/{userId}/income/{id}
    @Nested
    @DisplayName("DELETE /api/users/{userId}/income/{id}")
    class DeleteIncome {
        @Test
        @DisplayName("Returns 204 when income deleted")
        void deleteIncome_returns_204() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.findById(10L)).thenReturn(Optional.of(testIncome));
            doNothing().when(incomeRepository).deleteById(10L);

            mockMvc.perform(delete("/api/users/1/income/10"))
                    .andExpect(status().isNoContent());

            verify(incomeRepository).deleteById(10L);
        }

        @Test
        @DisplayName("Returns 404 when income belongs to a different user")
        void deleteIncome_returns_404_when_wrong_user() throws Exception {
            User otherUser = new User("other@example.com", "John", "Doe", LocalDate.of(1985, 1, 1));
            otherUser.setId(2L);

            Income otherIncome = new Income(otherUser, new BigDecimal("3000"), "Freelance", LocalDate.now());
            otherIncome.setId(20L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(incomeRepository.findById(20L)).thenReturn(Optional.of(otherIncome));

            mockMvc.perform(delete("/api/users/1/income/20"))
                    .andExpect(status().isNotFound());

            verify(incomeRepository, never()).deleteById(any());
        }
    }
}