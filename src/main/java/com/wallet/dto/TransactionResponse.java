package com.wallet.dto;

import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    
    private String transactionId;
    private String customerId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private TransactionStatus status;
    private String reference;
    private LocalDateTime timestamp;

    public TransactionResponse() {
    }

    public TransactionResponse(String transactionId, String customerId, TransactionType type,
                              BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter,
                              TransactionStatus status, String reference, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.reference = reference;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}