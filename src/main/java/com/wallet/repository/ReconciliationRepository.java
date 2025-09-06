package com.wallet.repository;

import com.wallet.entity.ReconciliationRecord;
import com.wallet.entity.ReconciliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReconciliationRepository extends JpaRepository<ReconciliationRecord, Long> {

    List<ReconciliationRecord> findByReconciliationDate(LocalDate date);

    List<ReconciliationRecord> findByReconciliationDateAndStatus(LocalDate date, ReconciliationStatus status);

    @Query("SELECT r FROM ReconciliationRecord r WHERE r.reconciliationDate >= :startDate AND r.reconciliationDate <= :endDate")
    List<ReconciliationRecord> findByDateRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    boolean existsByInternalTransactionId(String transactionId);

    boolean existsByExternalTransactionId(String transactionId);
}