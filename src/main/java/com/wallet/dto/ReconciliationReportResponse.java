package com.wallet.dto;

import com.wallet.entity.ReconciliationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReconciliationReportResponse {
    private LocalDate reconciliationDate;
    private ReconciliationSummary summary;
    private List<ReconciliationDetail> details;

    public ReconciliationReportResponse() {
    }

    public ReconciliationReportResponse(LocalDate reconciliationDate, ReconciliationSummary summary, 
                                      List<ReconciliationDetail> details) {
        this.reconciliationDate = reconciliationDate;
        this.summary = summary;
        this.details = details;
    }

    public LocalDate getReconciliationDate() {
        return reconciliationDate;
    }

    public void setReconciliationDate(LocalDate reconciliationDate) {
        this.reconciliationDate = reconciliationDate;
    }

    public ReconciliationSummary getSummary() {
        return summary;
    }

    public void setSummary(ReconciliationSummary summary) {
        this.summary = summary;
    }

    public List<ReconciliationDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ReconciliationDetail> details) {
        this.details = details;
    }

    public static class ReconciliationSummary {
        private int totalRecords;
        private int matchedRecords;
        private int missingInternalRecords;
        private int missingExternalRecords;
        private int amountMismatchRecords;
        private BigDecimal totalInternalAmount;
        private BigDecimal totalExternalAmount;
        private BigDecimal discrepancyAmount;

        public ReconciliationSummary() {
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

        public BigDecimal getTotalInternalAmount() {
            return totalInternalAmount;
        }

        public void setTotalInternalAmount(BigDecimal totalInternalAmount) {
            this.totalInternalAmount = totalInternalAmount;
        }

        public BigDecimal getTotalExternalAmount() {
            return totalExternalAmount;
        }

        public void setTotalExternalAmount(BigDecimal totalExternalAmount) {
            this.totalExternalAmount = totalExternalAmount;
        }

        public BigDecimal getDiscrepancyAmount() {
            return discrepancyAmount;
        }

        public void setDiscrepancyAmount(BigDecimal discrepancyAmount) {
            this.discrepancyAmount = discrepancyAmount;
        }
    }

    public static class ReconciliationDetail {
        private String internalTransactionId;
        private String externalTransactionId;
        private BigDecimal internalAmount;
        private BigDecimal externalAmount;
        private ReconciliationStatus status;
        private String notes;

        public ReconciliationDetail() {
        }

        public String getInternalTransactionId() {
            return internalTransactionId;
        }

        public void setInternalTransactionId(String internalTransactionId) {
            this.internalTransactionId = internalTransactionId;
        }

        public String getExternalTransactionId() {
            return externalTransactionId;
        }

        public void setExternalTransactionId(String externalTransactionId) {
            this.externalTransactionId = externalTransactionId;
        }

        public BigDecimal getInternalAmount() {
            return internalAmount;
        }

        public void setInternalAmount(BigDecimal internalAmount) {
            this.internalAmount = internalAmount;
        }

        public BigDecimal getExternalAmount() {
            return externalAmount;
        }

        public void setExternalAmount(BigDecimal externalAmount) {
            this.externalAmount = externalAmount;
        }

        public ReconciliationStatus getStatus() {
            return status;
        }

        public void setStatus(ReconciliationStatus status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}