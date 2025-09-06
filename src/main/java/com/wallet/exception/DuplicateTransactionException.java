package com.wallet.exception;

public class DuplicateTransactionException extends WalletException {
    
    public DuplicateTransactionException(String message) {
        super(message);
    }
}