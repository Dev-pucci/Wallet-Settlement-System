package com.wallet.service;

import com.wallet.entity.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransactionMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMessagingService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${wallet.rabbitmq.exchanges.wallet-exchange}")
    private String walletExchange;

    @Value("${wallet.rabbitmq.routing-keys.transaction}")
    private String transactionRoutingKey;

    public void sendTransactionMessage(Transaction transaction) {
        try {
            TransactionMessage message = new TransactionMessage(
                transaction.getTransactionId(),
                transaction.getWallet().getCustomerId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getStatus().name(),
                transaction.getReference(),
                transaction.getCreatedAt()
            );
            
            String messageJson = objectMapper.writeValueAsString(message);
            
            rabbitTemplate.convertAndSend(walletExchange, transactionRoutingKey, messageJson);
            
            logger.info("Transaction message sent for: {}", transaction.getTransactionId());
            
        } catch (Exception e) {
            logger.error("Failed to send transaction message: {}", transaction.getTransactionId(), e);
        }
    }

    public static class TransactionMessage {
        private String transactionId;
        private String customerId;
        private String type;
        private Object amount;
        private Object balanceBefore;
        private Object balanceAfter;
        private String status;
        private String reference;
        private Object timestamp;

        public TransactionMessage() {
        }

        public TransactionMessage(String transactionId, String customerId, String type,
                                Object amount, Object balanceBefore, Object balanceAfter,
                                String status, String reference, Object timestamp) {
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

        // Getters and setters
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getAmount() {
            return amount;
        }

        public void setAmount(Object amount) {
            this.amount = amount;
        }

        public Object getBalanceBefore() {
            return balanceBefore;
        }

        public void setBalanceBefore(Object balanceBefore) {
            this.balanceBefore = balanceBefore;
        }

        public Object getBalanceAfter() {
            return balanceAfter;
        }

        public void setBalanceAfter(Object balanceAfter) {
            this.balanceAfter = balanceAfter;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public Object getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Object timestamp) {
            this.timestamp = timestamp;
        }
    }
}