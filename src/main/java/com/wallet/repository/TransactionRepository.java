package com.wallet.repository;

import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    List<Transaction> findByWalletId(Long walletId);

    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<Transaction> findByTypeAndDateRange(@Param("type") TransactionType type,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}