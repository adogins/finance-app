package com.financeapp.dto;

import com.financeapp.entity.IncomeAllocation.AllocationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IncomeAllocationDto {
    public static class Request {
        private String category;
        private AllocationType allocationType;
        private BigDecimal allocationValue;
        private Integer priority;

        public Request() {}

        public String getCategory() { 
            return category;
        }

        public AllocationType getAllocationType() {
            return allocationType;
        }

        public BigDecimal getAllocationValue() {
            return allocationValue;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setAllocationType(AllocationType allocationType) {
            this.allocationType = allocationType;
        }

        public void setAllocationValue(BigDecimal allocationValue) {
            this.allocationValue = allocationValue;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }

    public static class Response {
        private Long id;
        private Long userId;
        private String category;
        private AllocationType allocationType;
        private BigDecimal allocationValue;
        private Integer priority;
        private LocalDateTime createdAt;

        public Response() {}

        public Long getId() {
            return id;
        }
        public Long getUserId() {
            return userId;
        }
        
        public String getCategory() {
            return category;
        }
        public AllocationType getAllocationType() {
            return allocationType;
        }

        public BigDecimal getAllocationValue() {
            return allocationValue;
        }

        public Integer getPriority() {
            return priority;
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

        public void setCategory(String category) {
            this.category = category;
        }

        public void setAllocationType(AllocationType allocationType) {
            this.allocationType = allocationType;
        }

        public void setAllocationValue(BigDecimal allocationValue) {
            this.allocationValue = allocationValue;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
    }
}