package com.wallet.service;

import com.wallet.dto.ExternalTransactionRecord;
import com.wallet.dto.ReconciliationReportResponse;
import com.wallet.dto.ReconciliationReportResponse.ReconciliationDetail;
import com.wallet.dto.ReconciliationReportResponse.ReconciliationSummary;
import com.wallet.entity.ReconciliationRecord;
import com.wallet.entity.ReconciliationStatus;
import com.wallet.entity.Transaction;
import com.wallet.repository.ReconciliationRepository;
import com.wallet.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReconciliationService {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReconciliationRepository reconciliationRepository;

    @Autowired
    private ExternalDataService externalDataService;

    @Autowired
    private ReconciliationMessagingService messagingService;

    public ReconciliationReportResponse generateReconciliationReport(LocalDate date) {
        logger.info("Generating reconciliation report for date: {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Transaction> internalTransactions = transactionRepository.findByDateRange(startOfDay, endOfDay);
        List<ExternalTransactionRecord> externalTransactions = externalDataService.getExternalTransactions(date);

        List<ReconciliationRecord> reconciliationRecords = performReconciliation(date, internalTransactions, externalTransactions);

        ReconciliationSummary summary = generateSummary(reconciliationRecords, internalTransactions, externalTransactions);
        List<ReconciliationDetail> details = convertToDetails(reconciliationRecords);

        logger.info("Reconciliation report generated for date: {} with {} records", date, reconciliationRecords.size());
        
        ReconciliationReportResponse response = new ReconciliationReportResponse(date, summary, details);
        
        // Send reconciliation report to message queue
        messagingService.sendReconciliationReport(response);
        
        return response;
    }

    @Scheduled(cron = "${wallet.reconciliation.schedule.cron}")
    public void performDailyReconciliation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        logger.info("Starting daily reconciliation for: {}", yesterday);
        
        try {
            generateReconciliationReport(yesterday);
            logger.info("Daily reconciliation completed successfully for: {}", yesterday);
        } catch (Exception e) {
            logger.error("Daily reconciliation failed for: {}", yesterday, e);
        }
    }

    private List<ReconciliationRecord> performReconciliation(LocalDate date, 
                                                           List<Transaction> internalTransactions,
                                                           List<ExternalTransactionRecord> externalTransactions) {
        
        List<ReconciliationRecord> records = new ArrayList<>();
        
        Map<String, Transaction> internalMap = internalTransactions.stream()
                .collect(Collectors.toMap(Transaction::getTransactionId, t -> t));
        
        Map<String, ExternalTransactionRecord> externalMap = externalTransactions.stream()
                .collect(Collectors.toMap(ExternalTransactionRecord::getTransactionId, t -> t));

        for (Transaction internal : internalTransactions) {
            ExternalTransactionRecord external = externalMap.get(internal.getTransactionId());
            
            if (external == null) {
                ReconciliationRecord record = new ReconciliationRecord(
                    date,
                    internal.getTransactionId(),
                    null,
                    internal.getAmount(),
                    null,
                    ReconciliationStatus.MISSING_EXTERNAL
                );
                record.setNotes("Internal transaction not found in external system");
                records.add(record);
            } else {
                ReconciliationStatus status = ReconciliationStatus.MATCHED;
                String notes = null;
                
                if (internal.getAmount().compareTo(external.getAmount()) != 0) {
                    status = ReconciliationStatus.AMOUNT_MISMATCH;
                    notes = String.format("Amount mismatch - Internal: %s, External: %s", 
                                        internal.getAmount(), external.getAmount());
                }
                
                ReconciliationRecord record = new ReconciliationRecord(
                    date,
                    internal.getTransactionId(),
                    external.getTransactionId(),
                    internal.getAmount(),
                    external.getAmount(),
                    status
                );
                record.setNotes(notes);
                records.add(record);
            }
        }

        for (ExternalTransactionRecord external : externalTransactions) {
            if (!internalMap.containsKey(external.getTransactionId())) {
                ReconciliationRecord record = new ReconciliationRecord(
                    date,
                    null,
                    external.getTransactionId(),
                    null,
                    external.getAmount(),
                    ReconciliationStatus.MISSING_INTERNAL
                );
                record.setNotes("External transaction not found in internal system");
                records.add(record);
            }
        }

        return reconciliationRepository.saveAll(records);
    }

    private ReconciliationSummary generateSummary(List<ReconciliationRecord> records, 
                                                 List<Transaction> internalTransactions,
                                                 List<ExternalTransactionRecord> externalTransactions) {
        ReconciliationSummary summary = new ReconciliationSummary();
        
        summary.setTotalRecords(records.size());
        summary.setMatchedRecords((int) records.stream().filter(r -> r.getStatus() == ReconciliationStatus.MATCHED).count());
        summary.setMissingInternalRecords((int) records.stream().filter(r -> r.getStatus() == ReconciliationStatus.MISSING_INTERNAL).count());
        summary.setMissingExternalRecords((int) records.stream().filter(r -> r.getStatus() == ReconciliationStatus.MISSING_EXTERNAL).count());
        summary.setAmountMismatchRecords((int) records.stream().filter(r -> r.getStatus() == ReconciliationStatus.AMOUNT_MISMATCH).count());
        
        BigDecimal totalInternal = internalTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExternal = externalTransactions.stream()
                .map(ExternalTransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.setTotalInternalAmount(totalInternal);
        summary.setTotalExternalAmount(totalExternal);
        summary.setDiscrepancyAmount(totalInternal.subtract(totalExternal).abs());
        
        return summary;
    }

    private List<ReconciliationDetail> convertToDetails(List<ReconciliationRecord> records) {
        return records.stream().map(record -> {
            ReconciliationDetail detail = new ReconciliationDetail();
            detail.setInternalTransactionId(record.getInternalTransactionId());
            detail.setExternalTransactionId(record.getExternalTransactionId());
            detail.setInternalAmount(record.getInternalAmount());
            detail.setExternalAmount(record.getExternalAmount());
            detail.setStatus(record.getStatus());
            detail.setNotes(record.getNotes());
            return detail;
        }).collect(Collectors.toList());
    }

    public List<ReconciliationRecord> getReconciliationRecords(LocalDate date) {
        return reconciliationRepository.findByReconciliationDate(date);
    }

    public List<ReconciliationRecord> getMismatchedRecords(LocalDate date) {
        return reconciliationRepository.findByReconciliationDateAndStatus(date, ReconciliationStatus.AMOUNT_MISMATCH);
    }
}