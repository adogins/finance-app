package com.financeapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SnapshotDto {
    public static class Response {
        private Long id;
        private Long userId;
        private LocalDate snapshotDate;
        private BigDecimal totalAssets;
        private BigDecimal totalLiabilities;
        private BigDecimal netWorth;
        private LocalDateTime createdAt;

        public Response() {}

        public Long getId() {
            return id;
        }

        public Long getUserId() {
            return userId;
        }

        public LocalDate getSnapshotDate() {
            return snapshotDate;
        }

        public BigDecimal getTotalAssets() {
            return totalAssets;
        }

        public BigDecimal getTotalLiabilities() {
            return totalLiabilities;
        }   

        public BigDecimal getNetWorth() {
            return netWorth;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public void setSnapshotDate(LocalDate snapshotDate) {
            this.snapshotDate = snapshotDate;
        }

        public void setTotalAssets(BigDecimal totalAssets) {
            this.totalAssets = totalAssets;
        }

        public void setTotalLiabilities(BigDecimal totalLiabilities) {
            this.totalLiabilities = totalLiabilities;
        }

        public void setNetWorth(BigDecimal netWorth) {
            this.netWorth = netWorth;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}