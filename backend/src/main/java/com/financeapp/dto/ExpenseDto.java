package com.financeapp.dto;

import math.BigDecimal;
import java.time.Local;
import java.time.LocalDateTime;

public class ExpenseDto {
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate spentAt;

    public Request() {}

    BigDecimal getAmount() {
        return amount;
    }
    
    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getSpentAt() {
        return spentAt;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSpentAt(LocalDate spentAt) {
        this.spentAt = spentAt;
    }

    public static class Response {
        private Long id;
        private Ling userId;
        private BigDecimal amount;
        private String category;
        private String description;
        private LocalDate spentAt;
        private LocalDateTime createdAt;

        public Response() {}

        public Long getId() {
            return id;
        }

        public Long getUserId() {
            return userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        public LocalDate getSpentAt() {
            return spentAt;
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

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setSpentAt(LocalDate spentAt) {
            this.spentAt = spentAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}