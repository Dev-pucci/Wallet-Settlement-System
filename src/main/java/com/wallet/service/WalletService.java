package com.wallet.service;

import com.wallet.dto.TransactionRequest;
import com.wallet.dto.TransactionResponse;
import com.wallet.dto.WalletBalanceResponse;
import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.Wallet;
import com.wallet.exception.DuplicateTransactionException;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.WalletNotFoundException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMessagingService messagingService;

    @Retryable(retryFor = {OptimisticLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransactionResponse topup(String customerId, TransactionRequest request) {
        logger.info("Processing topup for customer: {} with transaction: {}", customerId, request.getTransactionId());

        if (transactionRepository.existsByTransactionId(request.getTransactionId())) {
            throw new DuplicateTransactionException("Transaction ID already exists: " + request.getTransactionId());
        }

        Wallet wallet = getOrCreateWallet(customerId);
        BigDecimal balanceBefore = wallet.getBalance();

        wallet.topup(request.getAmount());
        
        Transaction transaction = new Transaction(
            request.getTransactionId(),
            wallet,
            TransactionType.TOPUP,
            request.getAmount(),
            balanceBefore,
            wallet.getBalance()
        );
        transaction.setReference(request.getReference());
        transaction.setStatus(TransactionStatus.COMPLETED);

        walletRepository.save(wallet);
        transaction = transactionRepository.save(transaction);

        TransactionResponse response = createTransactionResponse(transaction, customerId);
        
        messagingService.sendTransactionMessage(transaction);
        
        logger.info("Topup completed for customer: {} with new balance: {}", customerId, wallet.getBalance());
        return response;
    }

    @Retryable(retryFor = {OptimisticLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransactionResponse consume(String customerId, TransactionRequest request) {
        logger.info("Processing consume for customer: {} with transaction: {}", customerId, request.getTransactionId());

        if (transactionRepository.existsByTransactionId(request.getTransactionId())) {
            throw new DuplicateTransactionException("Transaction ID already exists: " + request.getTransactionId());
        }

        Wallet wallet = findWallet(customerId);
        BigDecimal balanceBefore = wallet.getBalance();

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                String.format("Insufficient balance. Available: %s, Required: %s", 
                    wallet.getBalance(), request.getAmount())
            );
        }

        wallet.consume(request.getAmount());

        Transaction transaction = new Transaction(
            request.getTransactionId(),
            wallet,
            TransactionType.CONSUME,
            request.getAmount(),
            balanceBefore,
            wallet.getBalance()
        );
        transaction.setReference(request.getReference());
        transaction.setStatus(TransactionStatus.COMPLETED);

        walletRepository.save(wallet);
        transaction = transactionRepository.save(transaction);

        TransactionResponse response = createTransactionResponse(transaction, customerId);
        
        messagingService.sendTransactionMessage(transaction);
        
        logger.info("Consume completed for customer: {} with new balance: {}", customerId, wallet.getBalance());
        return response;
    }

    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(String customerId) {
        logger.debug("Getting balance for customer: {}", customerId);
        
        Optional<Wallet> walletOpt = walletRepository.findByCustomerId(customerId);
        if (walletOpt.isEmpty()) {
            return new WalletBalanceResponse(customerId, BigDecimal.ZERO);
        }
        
        Wallet wallet = walletOpt.get();
        return new WalletBalanceResponse(customerId, wallet.getBalance());
    }

    private Wallet getOrCreateWallet(String customerId) {
        Optional<Wallet> walletOpt = walletRepository.findByCustomerIdWithLock(customerId);
        
        if (walletOpt.isPresent()) {
            return walletOpt.get();
        }

        logger.info("Creating new wallet for customer: {}", customerId);
        Wallet newWallet = new Wallet(customerId);
        return walletRepository.save(newWallet);
    }

    private Wallet findWallet(String customerId) {
        return walletRepository.findByCustomerIdWithLock(customerId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for customer: " + customerId));
    }

    private TransactionResponse createTransactionResponse(Transaction transaction, String customerId) {
        return new TransactionResponse(
            transaction.getTransactionId(),
            customerId,
            transaction.getType(),
            transaction.getAmount(),
            transaction.getBalanceBefore(),
            transaction.getBalanceAfter(),
            transaction.getStatus(),
            transaction.getReference(),
            transaction.getCreatedAt()
        );
    }
}