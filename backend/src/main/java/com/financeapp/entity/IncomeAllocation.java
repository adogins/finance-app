package com.financeapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "income_allocations")
public class IncomeAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_type", nullable = false)
    private AllocationType allocationType;

    @Column(name = "allocation_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal allocationValue;

    @Column(nullable = false)
    private Integer priority;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AllocationType {
        PERCENT, FIXED
    }

    public IncomeAllocation() {
    }

    public IncomeAllocation(User user, String category, AllocationType allocationType, BigDecimal allocationValue,
            Integer priority) {
        this.user = user;
        this.category = category;
        this.allocationType = allocationType;
        this.allocationValue = allocationValue;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public void setUser(User user) {
        this.user = user;
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