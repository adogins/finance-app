package com.financeapp.repository;

import com.financeapp.entity.Asset;
import com.financeapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for AssetRepository.
 * 
 * Tests custon sum queries for total assets and total liquid assets
 */
@DataJpaTest
class AssetRepositoryTest {

    Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AssetRepository assetRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("user1@example.com", "John", "Doe", LocalDate.of(1990, 1, 1));
        user2 = new User("user2@example.com", "Jane", "Smith", LocalDate.of(1985, 1, 1));

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByUserId returns only assets for specified user")
    void findByUserId_returns_only_user_assets() {
        Asset asset1 = new Asset(user1, "Checking", "Savings", new BigDecimal("10000"));
        Asset asset2 = new Asset(user1, "Stocks", "Investment", new BigDecimal("50000"));
        Asset asset3 = new Asset(user2, "401k", "Retirement", new BigDecimal("100000"));

        entityManager.persist(asset1);
        entityManager.persist(asset2);
        entityManager.persist(asset3);
        entityManager.flush();

        List<Asset> result = assetRepository.findByUserId(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Asset::getName).containsExactlyInAnyOrder("Checking", "Stocks");
    }

    @Test
    @DisplayName("sumBalanceByUserId calculates total of all asset types")
    void sumBalanceByUserId_sums_all_assets() {
        Asset savings = new Asset(user1, "Savings", "Savings", new BigDecimal("20000"));
        Asset stocks = new Asset(user1, "Brokerage", "Investment", new BigDecimal("80000"));
        Asset property = new Asset(user1, "Home", "Property", new BigDecimal("400000"));

        entityManager.persist(savings);
        entityManager.persist(stocks);
        entityManager.persist(property);
        entityManager.flush();

        BigDecimal total = assetRepository.sumBalanceByUserId(user1.getId());
        assertThat(total).isEqualByComparingTo("500000.00");
    }

    @Test
    @DisplayName("sumLiquidAssetsByUserId sums only Savings type assets")
    void sumLiquidAssetsByUserId_sums_only_savings() {
        Asset checking = new Asset(user1, "Checking", "Savings", new BigDecimal("5000"));
        Asset savingsAccount = new Asset(user1, "High Yield", "Savings", new BigDecimal("15000"));
        Asset stocks = new Asset(user1, "Brokerage", "Investment", new BigDecimal("50000"));
        Asset house = new Asset(user1, "Home", "Property", new BigDecimal("400000"));

        entityManager.persist(checking);
        entityManager.persist(savingsAccount);  
        entityManager.persist(stocks);
        entityManager.persist(house);
        entityManager.flush();

        BigDecimal liquid = assetRepository.sumLiquidAssetsByUserId(user1.getId());

        // Should only sum the two "Savings" type assets
        assertThat(liquid).isEqualByComparingTo("20000.00");
    }

    @Test
    @DisplayName("sumBalanceByUserId returns zero when no assets exist")
    void sumBalanceByUserId_returns_zero_when_empty() {
        BigDecimal total = assetRepository.sumBalanceByUserId(user1.getId());
        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("findByUserIdAndType filters assets by type")
    void findByUserIdAndType_filters_correctly() {
        Asset savings1 = new Asset(user1, "Checking", "Savings", new BigDecimal("5000"));
        Asset savings2 = new Asset(user1, "Money Market", "Savings", new BigDecimal("10000"));
        Asset investment = new Asset(user1, "Stocks", "Investment", new BigDecimal("50000"));

        entityManager.persist(savings1);
        entityManager.persist(savings2);
        entityManager.persist(investment);
        entityManager.flush();

        List<Asset> savingsAssets = assetRepository.findByUserIdAndType(user1.getId(), "Savings");

        assertThat(savingsAssets).hasSize(2);
        assertThat(savingsAssets).extracting(Asset::getType).containsOnly("Savings");
    }
}