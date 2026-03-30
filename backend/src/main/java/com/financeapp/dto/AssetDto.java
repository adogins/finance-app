package com.financeapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AssetDto {
    public static class Request {
        private String name;
        private String type;
        private BigDecimal balance;

        public Request() {
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }

    public static class Response {
        private Long id;
        private Long userId;
        private String name;
        private String type;
        private BigDecimal balance;
        private LocalDateTime updatedAt;

        public Response() {
        }

        public Long getId() {
            return id;
        }

        public Long getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}