package com.wallet.controller;

import com.wallet.dto.TransactionRequest;
import com.wallet.dto.TransactionResponse;
import com.wallet.dto.WalletBalanceResponse;
import com.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private WalletService walletService;

    @GetMapping("/{customerId}/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable String customerId) {
        logger.info("Getting balance for customer: {}", customerId);
        WalletBalanceResponse response = walletService.getBalance(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{customerId}/topup")
    public ResponseEntity<TransactionResponse> topup(
            @PathVariable String customerId,
            @Valid @RequestBody TransactionRequest request) {
        
        logger.info("Processing topup request for customer: {} with amount: {}", customerId, request.getAmount());
        TransactionResponse response = walletService.topup(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{customerId}/consume")
    public ResponseEntity<TransactionResponse> consume(
            @PathVariable String customerId,
            @Valid @RequestBody TransactionRequest request) {
        
        logger.info("Processing consume request for customer: {} with amount: {}", customerId, request.getAmount());
        TransactionResponse response = walletService.consume(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}