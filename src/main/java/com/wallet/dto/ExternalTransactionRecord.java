package com.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExternalTransactionRecord {
    private String transactionId;
    private String customerId;
    private BigDecimal amount;
    private String type;
    private LocalDateTime timestamp;
    private String reference;

    public ExternalTransactionRecord() {
    }

    public ExternalTransactionRecord(String transactionId, String customerId, BigDecimal amount, 
                                   String type, LocalDateTime timestamp, String reference) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.reference = reference;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}