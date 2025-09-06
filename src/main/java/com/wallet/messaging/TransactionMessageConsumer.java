package com.wallet.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.service.TransactionMessagingService.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "${wallet.rabbitmq.queues.transaction-processing}")
    public void processTransaction(String messageJson) {
        try {
            logger.info("Received transaction message: {}", messageJson);
            
            TransactionMessage message = objectMapper.readValue(messageJson, TransactionMessage.class);
            
            processTransactionMessage(message);
            
            logger.info("Successfully processed transaction: {}", message.getTransactionId());
            
        } catch (Exception e) {
            logger.error("Failed to process transaction message: {}", messageJson, e);
            throw new RuntimeException("Failed to process transaction message", e);
        }
    }

    private void processTransactionMessage(TransactionMessage message) {
        logger.debug("Processing transaction message for: {} of type: {} with amount: {}", 
                    message.getTransactionId(), message.getType(), message.getAmount());
        
        switch (message.getType()) {
            case "TOPUP":
                processTopupMessage(message);
                break;
            case "CONSUME":
                processConsumeMessage(message);
                break;
            default:
                logger.warn("Unknown transaction type: {}", message.getType());
        }
    }

    private void processTopupMessage(TransactionMessage message) {
        logger.info("Processing topup message for customer: {} with amount: {}", 
                   message.getCustomerId(), message.getAmount());
    }

    private void processConsumeMessage(TransactionMessage message) {
        logger.info("Processing consume message for customer: {} with amount: {}", 
                   message.getCustomerId(), message.getAmount());
    }
}