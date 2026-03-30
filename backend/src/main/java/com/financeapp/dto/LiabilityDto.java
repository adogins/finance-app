package com.financeapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LiabilityDto {
    public static class Request {
        private String name;
        private String type;
        private BigDecimal balance;
        private BigDecimal interestRate;
        private BigDecimal monthlyPayment;

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

        public BigDecimal getInterestRate() {
            return interestRate;
        }

        public BigDecimal getMonthlyPayment() {
            return monthlyPayment;
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

        public void setInterestRate(BigDecimal interestRate) {
            this.interestRate = interestRate;
        }

        public void setMonthlyPayment(BigDecimal monthlyPayment) {
            this.monthlyPayment = monthlyPayment;
        }
    }

    public static class Response {
        private Long id;
        private Long userId;
        private String name;
        private String type;
        private BigDecimal balance;
        private BigDecimal interestRate;
        private BigDecimal monthlyPayment;
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

        public BigDecimal getInterestRate() {
            return interestRate;
        }

        public BigDecimal getMonthlyPayment() {
            return monthlyPayment;
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

        public void setInterestRate(BigDecimal interestRate) {
            this.interestRate = interestRate;
        }

        public void setMonthlyPayment(BigDecimal monthlyPayment) {
            this.monthlyPayment = monthlyPayment;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}