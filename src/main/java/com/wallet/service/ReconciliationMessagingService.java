package com.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.ReconciliationReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReconciliationMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationMessagingService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${wallet.rabbitmq.exchanges.wallet-exchange}")
    private String walletExchange;

    @Value("${wallet.rabbitmq.routing-keys.reconciliation}")
    private String reconciliationRoutingKey;

    public void sendReconciliationReport(ReconciliationReportResponse report) {
        try {
            ReconciliationReportMessage message = new ReconciliationReportMessage(
                report.getReconciliationDate().toString(),
                report.getSummary().getTotalRecords(),
                report.getSummary().getMatchedRecords(),
                report.getSummary().getMissingInternalRecords(),
                report.getSummary().getMissingExternalRecords(),
                report.getSummary().getAmountMismatchRecords(),
                report.getSummary().getTotalInternalAmount(),
                report.getSummary().getTotalExternalAmount(),
                report.getSummary().getDiscrepancyAmount(),
                System.currentTimeMillis()
            );
            
            String messageJson = objectMapper.writeValueAsString(message);
            
            rabbitTemplate.convertAndSend(walletExchange, reconciliationRoutingKey, messageJson);
            
            logger.info("Reconciliation report message sent for date: {}", report.getReconciliationDate());
            
        } catch (Exception e) {
            logger.error("Failed to send reconciliation report message for date: {}", report.getReconciliationDate(), e);
        }
    }

    public static class ReconciliationReportMessage {
        private String reconciliationDate;
        private int totalRecords;
        private int matchedRecords;
        private int missingInternalRecords;
        private int missingExternalRecords;
        private int amountMismatchRecords;
        private Object totalInternalAmount;
        private Object totalExternalAmount;
        private Object discrepancyAmount;
        private long timestamp;

        public ReconciliationReportMessage() {
        }

        public ReconciliationReportMessage(String reconciliationDate, int totalRecords, int matchedRecords, 
                                         int missingInternalRecords, int missingExternalRecords, 
                                         int amountMismatchRecords, Object totalInternalAmount, 
                                         Object totalExternalAmount, Object discrepancyAmount, long timestamp) {
            this.reconciliationDate = reconciliationDate;
            this.totalRecords = totalRecords;
            this.matchedRecords = matchedRecords;
            this.missingInternalRecords = missingInternalRecords;
            this.missingExternalRecords = missingExternalRecords;
            this.amountMismatchRecords = amountMismatchRecords;
            this.totalInternalAmount = totalInternalAmount;
            this.totalExternalAmount = totalExternalAmount;
            this.discrepancyAmount = discrepancyAmount;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getReconciliationDate() {
            return reconciliationDate;
        }

        public void setReconciliationDate(String reconciliationDate) {
            this.reconciliationDate = reconciliationDate;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getMatchedRecords() {
            return matchedRecords;
        }

        public void setMatchedRecords(int matchedRecords) {
            this.matchedRecords = matchedRecords;
        }

        public int getMissingInternalRecords() {
            return missingInternalRecords;
        }

        public void setMissingInternalRecords(int missingInternalRecords) {
            this.missingInternalRecords = missingInternalRecords;
        }

        public int getMissingExternalRecords() {
            return missingExternalRecords;
        }

        public void setMissingExternalRecords(int missingExternalRecords) {
            this.missingExternalRecords = missingExternalRecords;
        }

        public int getAmountMismatchRecords() {
            return amountMismatchRecords;
        }

        public void setAmountMismatchRecords(int amountMismatchRecords) {
            this.amountMismatchRecords = amountMismatchRecords;
        }

        public Object getTotalInternalAmount() {
            return totalInternalAmount;
        }

        public void setTotalInternalAmount(Object totalInternalAmount) {
            this.totalInternalAmount = totalInternalAmount;
        }

        public Object getTotalExternalAmount() {
            return totalExternalAmount;
        }

        public void setTotalExternalAmount(Object totalExternalAmount) {
            this.totalExternalAmount = totalExternalAmount;
        }

        public Object getDiscrepancyAmount() {
            return discrepancyAmount;
        }

        public void setDiscrepancyAmount(Object discrepancyAmount) {
            this.discrepancyAmount = discrepancyAmount;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}