package com.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletBalanceResponse {
    private String customerId;
    private BigDecimal balance;
    private LocalDateTime timestamp;

    public WalletBalanceResponse() {
    }

    public WalletBalanceResponse(String customerId, BigDecimal balance) {
        this.customerId = customerId;
        this.balance = balance;
        this.timestamp = LocalDateTime.now();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}