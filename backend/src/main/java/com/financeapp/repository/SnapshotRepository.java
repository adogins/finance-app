package com.financeapp.repository;

import com.financeapp.entity.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {

    // All snapshots for a user ordered by date ascending
    List<Snapshot> findByUserIdOrderBySnapshotDateAsc(Long userId);

    // Snapshot within a date range
    List<Snapshot> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(Long userId, LocalDate from, LocalDate to);

    // Most recent snapshot
    Optional<Snapshot> findTopByUserIdOrderBySnapshotDateDesc(Long userId);

    // Nearest snapshot on or before a given date
    Optional<Snapshot> findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(Long userId, LocalDate date);

    // Check if a snapshot already exists for a given date
    boolean existsByUserIdAndSnapshotDate(Long userId, LocalDate snapshotDate);
}