package com.financeapp.service;

import com.financeapp.entity.User;
import com.financeapp.repository.AssetRepository;
import com.financeapp.repository.ExpenseRepository;
import com.financeapp.repository.IncomeRepository;
import com.financeapp.repository.LiabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FinanceRatioService.
 * 
 * All repository calls are mocked - no database required.
 * Tests verify ratio values, status thresholds, and age bracket benchmarks.
 */
