package com.financeapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "retirement_accounts")
public class RetirementAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(length = 256)
    private String provider;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "monthly_contribution", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyContribution;

    @Column(name = "employer_match", precision = 15, scale = 2)
    private BigDecimal employerMatch;

    @Column(name = "expected_return_rate", precision = 5, scale = 2)
    private BigDecimal expectedReturnRate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RetirementAccount() {
    }

    public RetirementAccount(User user, String name, String provider, BigDecimal balance,
            BigDecimal monthlyContribution,
            BigDecimal employerMatch, BigDecimal expectedReturnRate) {
        this.user = user;
        this.name = name;
        this.provider = provider;
        this.balance = balance;
        this.monthlyContribution = monthlyContribution;
        this.employerMatch = employerMatch;
        this.expectedReturnRate = expectedReturnRate;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public void setUser(User user) {
        this.user = user;
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

    @Transient
    public BigDecimal getTotalMonthlyContribution() {
        if (employerMatch == null)
            return monthlyContribution;
        return monthlyContribution.add(employerMatch);
    }
}