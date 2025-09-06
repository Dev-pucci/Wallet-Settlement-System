package com.wallet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_records")
public class ReconciliationRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;
    
    @Column(name = "internal_transaction_id", length = 100)
    private String internalTransactionId;
    
    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;
    
    @Column(name = "internal_amount", precision = 19, scale = 2)
    private BigDecimal internalAmount;
    
    @Column(name = "external_amount", precision = 19, scale = 2)
    private BigDecimal externalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconciliationStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public ReconciliationRecord() {
    }
    
    public ReconciliationRecord(LocalDate reconciliationDate, String internalTransactionId,
                               String externalTransactionId, BigDecimal internalAmount,
                               BigDecimal externalAmount, ReconciliationStatus status) {
        this.reconciliationDate = reconciliationDate;
        this.internalTransactionId = internalTransactionId;
        this.externalTransactionId = externalTransactionId;
        this.internalAmount = internalAmount;
        this.externalAmount = externalAmount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getReconciliationDate() {
        return reconciliationDate;
    }
    
    public void setReconciliationDate(LocalDate reconciliationDate) {
        this.reconciliationDate = reconciliationDate;
    }
    
    public String getInternalTransactionId() {
        return internalTransactionId;
    }
    
    public void setInternalTransactionId(String internalTransactionId) {
        this.internalTransactionId = internalTransactionId;
    }
    
    public String getExternalTransactionId() {
        return externalTransactionId;
    }
    
    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }
    
    public BigDecimal getInternalAmount() {
        return internalAmount;
    }
    
    public void setInternalAmount(BigDecimal internalAmount) {
        this.internalAmount = internalAmount;
    }
    
    public BigDecimal getExternalAmount() {
        return externalAmount;
    }
    
    public void setExternalAmount(BigDecimal externalAmount) {
        this.externalAmount = externalAmount;
    }
    
    public ReconciliationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReconciliationStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}