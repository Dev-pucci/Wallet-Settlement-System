package com.wallet.repository;

import com.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByCustomerId(String customerId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT w FROM Wallet w WHERE w.customerId = :customerId")
    Optional<Wallet> findByCustomerIdWithLock(@Param("customerId") String customerId);

    boolean existsByCustomerId(String customerId);
}