package com.financeapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RetirementAccountDto {
    public statis class Request {
        private String name;
        private String provider;
        private BigDecimal balance;
        private BigDecimal monthlyContribution;
        private BigDecimal employerMatch;
        private BigDecimal expectedReturnRate;

        public Request() {}

        public String getName() {
            return name;
        }

        public String getProvider() {
            return provider;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public BigDecimal getMonthlyContribution() {
            return monthlyContribution;
        }

        public BigDecimal getEmployerMatch() {
            return employerMatch;
        }

        public BigDecimal getExpectedReturnRate() {
            return expectedReturnRate;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public void setMonthlyContribution(BigDecimal monthlyContribution) {
            this.monthlyContribution = monthlyContribution;
        }

        public void setEmployerMatch(BigDecimal employerMatch) {
            this.employerMatch = employerMatch;
        }

        public void setExpectedReturnRate(BigDecimal expectedReturnRate) {
            this.expectedReturnRate = expectedReturnRate;
        }
    }

    public static class Repsonse {
        private Long id;
        private Long userId;
        private String name;
        private String provider;
        private BigDecimal balance;
        private BigDecimal monthlyContribution;
        private BigDecimal employerMatch;
        private BigDecimal expectedReturnRate;
        private LocalDateTime updatedAt;

        public Response() {}

        public Long getId() {
            return id;
        }

        public Long getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getProvider() {
            return provider;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public BigDecimal getMonthlyContribution() {
            return monthlyContribution;
        }

        public BigDecimal getEmployerMatch() {
            return employerMatch;
        }

        public BigDecimal getExpectedReturnRate() {
            return expectedReturnRate;
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

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public void setMonthlyContribution(BigDecimal monthlyContribution) {
            this.monthlyContribution = monthlyContribution;
        }

        public void setEmployerMatch(BigDecimal employerMatch) {
            this.employerMatch = employerMatch;
        }

        public void setExpectedReturnRate(BigDecimal expectedReturnRate) {
            this.expectedReturnRate = expectedReturnRate;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}