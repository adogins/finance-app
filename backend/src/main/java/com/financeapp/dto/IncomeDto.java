package com.financeapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class IncomeDto {

    public static class Request {
        private BigDecimal amount;
        private String source;
        private LocalDate receivedAt;

        public Request() {
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getSource() {
            return source;
        }

        public LocalDate getReceivedAt() {
            return receivedAt;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public void setReceivedAt(LocalDate receivedAt) {
            this.receivedAt = receivedAt;
        }
    }

    public static class Response {
        private Long id;
        private Long userId;
        private BigDecimal amount;
        private String source;
        private LocalDate receivedAt;
        private LocalDateTime createdAt;

        public Response() {
        }

        public Long getId() {
            return id;
        }

        public Long getUserId() {
            return userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getSource() {
            return source;
        }

        public LocalDate getReceivedAt() {
            return receivedAt;
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

        public void setSource(String source) {
            this.source = source;
        }

        public void setReceivedAt(LocalDate receivedAt) {
            this.receivedAt = receivedAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}