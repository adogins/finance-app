package com.financeapp.repository;

import com.financeapp.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // All assets for a user
    List<Asset> findByUserId(Long userId);

    // Assets by type
    List<Asset> findByUserIdAndType(Long userId, String type);

    // Sum of all asset balances for a user
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Asset a WHERE a.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") Long userId);

    // Sum if liquid assets only
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Asset a WHERE a.user.id = :userId AND a.type = 'Savings'")
    BigDecimal sumLiquidAssetsByUserId(@Param("userId") Long userId);
}