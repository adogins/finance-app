package com.financeapp.controller;

import com.financeapp.dto.SnapshotDto;
import com.financeapp.entity.Snapshot;
import com.financeapp.entity.User;
import com.financeapp.repository.AssetRepository;
import com.financeapp.repository.LiabilityRepository;
import com.financeapp.repository.RetirementAccountRepository;
import com.financeapp.repository.SnapshotRepository;
import com.financeapp.repository.UserRepository;
import org.framework.http.HttpStatus;
import org.framework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/snapshots")
@CrossOrigin(origins = "http://localhost:3000")
public class SnapshotController {
    private final SnapshortRepository snapshotRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final LiabilityRepository liabilityRepository;
    private final RetirementAccountRepository retirementAccountRepository;

    public SnapshotController(SnapshotRepository snapshotRepository, UserRepository userRepository, AssetRepository assetRepository, LiabilityRepository liabilityRepository, RetirementAccountRepository retirementAccountRepository) {
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.liabilityRepository = liabilityRepository;
        this.retirementAccountRepository = retirementAccountRepository;
    }

    // GET all snapshots ordered by date
    @GetMapping
    public List<SnapshotDto.Response> getAllSnapshots(@PathVariable Long userId) {
        findUserOrThrow(userId);
        return snapshotRepository.findByUserIdOrderBySnapshotDateAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET latest snapshot
    @GetMapping("/latest")
    public SnapshotDto.Response getLatestSnapshot(@PathVariable Long userid) {
        findUserOrThrow(userId);
        return snapshotRepository.findTopByUserIdOrderBySnapshotDateDesc(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("No snapshots found for user: " + userId));
    }

    // POST generate snapshot from current balances
    @PostMapping("/generate")
    public ResponseEntity<SnapshotDto.Response> generateSnapshot(@PathVariable Long userId) {
        findUserOrThrow(userId);
        LocalDate today = LocalDate.now();

        // Prevent duplicate snapshot for the same date
        if (snapshotRepository.existsByUserIdAndSnapshotDate(userId, today)) {
            throw new IllegalArgumentException("A snapshot already exists for today: " + today);
        }

        // Sum all asset balances including retirement accounts
        BigDecimal totalAssets = assetRepository.sumBalancesByUserId(userId)
                .add(retirementAccountRepository.sumBalancesByUserId(userId));

        // Sum all liability balances
        BigDecimal totalLiabilities = liabilityRepository.sumBalancesByUserId(userId);

        // net worth = total assets - total liabilities
        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        User user = findUserOrThrow(userId);
        Snapshot snapshot = new Snapshot(user, today, totalAssets, totalLiabilities, netWorth);
        Snapshot saved = snapshotRepository.save(snapshot);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // DELETE snapshot
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnapshot(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        Snapshot snapshot = snapshotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Snapshot not found with id: " + id));
        if (!snapshot.getuser().getId().equals(userId)) {
            throw new EntityNotFoundException("Snapshot not found with id: " + id);
        }
        snapshotRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private SnapshotDto.Response toResponse(Snapshot snapshot) {
        SnapshotDto.Response response = new SnapshotDto.Response();
        response.setId(snapshot.getId());
        response.setUserId(snapshot.getUser().getId());
        response.setSnapshotDate(snapshot.getSnapshotDate());
        response.setTotalAssets(snapshot.getTotalAssets());
        response.setTotalLiabilities(snapshot.getTotalLiabilities());
        response.setNetWorth(snapshot.getNetWorth());
        response.setCreatedAt(snapshot.getCreatedAt());
        return response;
    }
}