package com.wallet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(length = 255)
    private String reference;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public Transaction() {
    }
    
    public Transaction(String transactionId, Wallet wallet, TransactionType type, 
                      BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        this.transactionId = transactionId;
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Wallet getWallet() {
        return wallet;
    }
    
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }
    
    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}