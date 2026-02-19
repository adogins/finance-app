package com.financeapp.controller;

import com.financeapp.entity.Snapshot;
import com.financeapp.entity.User;
import com.financecapp.repository.AssetRepository;
import com.financecapp.repository.LiabilityRepository;
import com.financecapp.repository.RetirementAccountRepository;
import com.financecapp.repository.SnapshotRepository;
import com.financecapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ogr.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 
 * Integration tests for SnapshotController.
 * 
 * Test snapshot generation logic and and duplicate date validation.
 */
@WebMvcTest(SnapshotController.class)
class SnapshotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SnapshotRepository snapshotRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AssetRepository assetRepository;

    @MockBean
    private LiabilityRepository liabilityRepository;

    @MockBean
    private RetirementAccountRepository retirementAccountRepository;

    private User testUser;
    private Snapshot testSnapshot;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "John", "Doe", LocalDate.of(1990, 5, 15));
        testUser.setId(1L);

        testSnapshot = new Snapshot(testUser, LocalDate.of(2025, 3, 1), new BigDecimal("500000"), new BigDecimal("200000"), new BigDecimal("300000"));
        testSnapshot.setId(10L);
    }

    // GET /api/users/{userId}/snapshots
    @Nested
    @DisplayName("GET /api/users/{userId}/snapshots")
    class GetAllSnapshots {
        @Test
        @DisplayName("Returns 200 with list of snapshots ordered by date")
        void getAllSnapshots_returns_200() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.findByUserIdOrderBySnapshotDateAsc(1L)).thenReturn(List.of(testSnapshot));

            mockMvc.perform(get("/api/users/1/snapshots"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(10))
                    .andExpect(jsonPath("$[0].userId").value(1))
                    .andExpect(jsonPath("$[0].totalAssets").value(500000))
                    .andExpect(jsonPath("$[0].totalLiabilities").value(200000))
                    .andExpect(jsonPath("$[0].netWorth").value(300000));
        }

        @Test
        @DisplayName("Returns 404 when user not found")
        void getAllSnapshots_returns_404_when_user_not_found() throws Exception {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/999/snapshots"))
                    .andExpect(status().isNotFound());
        }
    }

    // GET /api/users/{userId}/snapshots/latest
    @Nested
    @DisplayName("GET /api/users/{userId}/snapshots/latest")
    class GetLatestSnapshot {

        @Test
        @DisplayName("Returns 200 with most recent snapshot")
        void getLatestSnapshot_returns_200() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.findTopByUserIdOrderBySnapshotDateDesc(1L)).thenReturn(Optional.of(testSnapshot));

            mockMvc.perform(get("/api/users/1/snapshots/latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.netWorth").value(300000));
        }

        @Test
        @DisplayName("Returns 404 when no snapshots exist")
        void getLatestSnapshot_returns_404_when_none_exist() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.findTopByUserIdOrderBySnapshotDateDesc(1L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/1/snapshots/latest"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("No snapshots found")));
        }
    }

    // POST /api/users/{userId}/snapshots/generate
    @Nested
    @DisplayName("POST /api/users/{userId}/snapshots/generate")
    class GenerateSnapshot {
        @Test
        @DisplayName("Returns 201 with generated snapshot")
        void generateSnapshot_returns_201() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.existsByUserIdAndSnapshotDate(any(), any())).thenReturn(false);
            when(assetRepository.sumBalanceByUser(1L)).thenReturn(new BigDecimal("400000"));
            when(retirementAccountRepository.sumBalanceByUser(1L)).thenReturn(new BigDecimal("100000"));
            when(liabilityRepository.sumBalanceByUser(1L)).thenReturn(new BigDecimal("200000"));
            when(snapshotRepository.save(any(Snapshot.class))).thenReturn(testSnapshot);

            mockMvc.perform(post("/api/users/1/snapshots/generate"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalAssets").value(500000))
                    .andExpect(jsonPath("$.totalLiabilities").value(200000))
                    .andExpect(jsonPath("$.netWorth").value(300000));

            verify(snapshotRepository).save(any(Snapshot.class));
        }

        @Test
        @DisplayName("Returns 400 when snapshot already exists for today")
        void generateSnapshot_returns_400_when_duplicate() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.existsByUserIdAndSnapshotDate(eq(1L), any(LocalDate.class))).thenReturn(true);

            mockMvc.perform(post("/api/users/1/snapshots/generate"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("already exists for today")));

            verify(snapshotRepository, never()).save(any());
        }

        @Test
        @DisplayName("Includes retirement account balances in total assets")
        void generateSnapshot_includes_retirement_accounts() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.existsByUserIdAndSnapshotDate(any(), any())).thenReturn(false);
            when(assetRepository.sumBalanceByUser(1L)).thenReturn(new BigDecimal("300000"));
            when(retirementAccountRepository.sumBalanceByUser(1L)).thenReturn(new BigDecimal("150000"));
            when(liabilityRepository.sumBalanceByUser(1L)).thenReturn(new BigDecimal("100000"));

            Snapshot expected = new Snapshot(
                testUser,
                LocalDate.now(),
                new BigDecimal("450000"),  // 300k + 150k
                new BigDecimal("100000"), 
                new BigDecimal("350000")   // 450k - 100k
            );
            expected.setId(15L);

            when(snapshotRepository.save(any(Snapshot.class))).thenReturn(expected);

            mockMvc.perform(post("/api/users/1/snapshots/generate"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalAssets").value(450000))
                    .andExpect(jsonPath("$.netWorth").value(350000));
        }
    }

    // DELETE /api/users/{userId}/snapshots/{id}
    @Nested
    @DisplayName("DELETE /api/users/{userId}/snapshots/{id}")
    class DeleteSnapshot {
        @Test
        @DisplayName("Returns 204 when snapshot deleted")
        void deleteSnapshot_returns_204() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.findById(10L)).thenReturn(Optional.of(testSnapshot));
            doNothing().when(snapshotRepository).deleteById(10L);

            mockMvc.perform(delete("/api/users/1/snapshots/10"))
                    .andExpect(status().isNoContent());

            verify(snapshotRepository).deleteById(10L);
        }

        @Test
        @DisplayName("Returns 404 when snapshot belongs to different user")
        void deleteSnapshot_returns_404_when_wrong_user() throws Exception {
            User otherUser = new User("other@example.com", "Jane", "Smith", LocalDate.of(1985, 1, 1));
            otherUser.setId(2L);

            Snapshot otherSnapshot = new Snapshot(otherUser, LocalDate.now(), new BigDecimal("100000"), new BigDecimal("50000"), new BigDecimal("50000"));
            otherSnapshot.setId(20L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(snapshotRepository.findById(20L)).thenReturn(Optional.of(otherSnapshot));

            mockMvc.perform(delete("/api/users/1/snapshots/20"))
                    .andExpect(status().isNotFound());

            verify(snapshotRepository, never()).deleteById(any());
        }
    }
}