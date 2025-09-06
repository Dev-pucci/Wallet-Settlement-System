package com.wallet.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.service.ReconciliationMessagingService.ReconciliationReportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationMessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "${wallet.rabbitmq.queues.reconciliation-processing}")
    public void processReconciliationReport(String messageJson) {
        try {
            logger.info("Received reconciliation report message: {}", messageJson);
            
            ReconciliationReportMessage message = objectMapper.readValue(messageJson, ReconciliationReportMessage.class);
            
            processReconciliationMessage(message);
            
            logger.info("Successfully processed reconciliation report for date: {}", message.getReconciliationDate());
            
        } catch (Exception e) {
            logger.error("Failed to process reconciliation message: {}", messageJson, e);
            throw new RuntimeException("Failed to process reconciliation message", e);
        }
    }

    private void processReconciliationMessage(ReconciliationReportMessage message) {
        logger.info("Processing reconciliation report for date: {} with {} total records", 
                   message.getReconciliationDate(), message.getTotalRecords());
        
        if (message.getAmountMismatchRecords() > 0) {
            logger.warn("Found {} amount mismatches in reconciliation for date: {}", 
                       message.getAmountMismatchRecords(), message.getReconciliationDate());
        }
        
        if (message.getMissingInternalRecords() > 0) {
            logger.warn("Found {} missing internal records in reconciliation for date: {}", 
                       message.getMissingInternalRecords(), message.getReconciliationDate());
        }
        
        if (message.getMissingExternalRecords() > 0) {
            logger.warn("Found {} missing external records in reconciliation for date: {}", 
                       message.getMissingExternalRecords(), message.getReconciliationDate());
        }
        
        if (message.getMatchedRecords() == message.getTotalRecords()) {
            logger.info("Perfect reconciliation - all {} records matched for date: {}", 
                       message.getTotalRecords(), message.getReconciliationDate());
        }
        
        // Here you could add additional processing like:
        // - Sending alerts for mismatches
        // - Updating external monitoring systems
        // - Triggering corrective actions
        // - Storing reconciliation metrics
    }
}