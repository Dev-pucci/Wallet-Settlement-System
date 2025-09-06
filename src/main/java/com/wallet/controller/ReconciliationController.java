package com.wallet.controller;

import com.wallet.dto.ReconciliationReportResponse;
import com.wallet.service.ExternalDataService;
import com.wallet.service.ReconciliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/reconciliation")
public class ReconciliationController {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationController.class);

    @Autowired
    private ReconciliationService reconciliationService;

    @Autowired
    private ExternalDataService externalDataService;

    @GetMapping("/report")
    public ResponseEntity<ReconciliationReportResponse> getReconciliationReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        logger.info("Generating reconciliation report for date: {}", date);
        
        ReconciliationReportResponse report = reconciliationService.generateReconciliationReport(date);
        
        return ResponseEntity.ok(report);
    }

    @GetMapping("/report/export")
    public void exportReconciliationReportToCsv(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletResponse response) throws IOException {
        
        logger.info("Exporting reconciliation report to CSV for date: {}", date);
        
        ReconciliationReportResponse report = reconciliationService.generateReconciliationReport(date);
        
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                          "attachment; filename=reconciliation_report_" + date + ".csv");
        
        try (var outputStream = response.getOutputStream()) {
            outputStream.write("Reconciliation Report\n".getBytes());
            outputStream.write(("Date: " + date + "\n").getBytes());
            outputStream.write(("Total Records: " + report.getSummary().getTotalRecords() + "\n").getBytes());
            outputStream.write(("Matched: " + report.getSummary().getMatchedRecords() + "\n").getBytes());
            outputStream.write(("Missing Internal: " + report.getSummary().getMissingInternalRecords() + "\n").getBytes());
            outputStream.write(("Missing External: " + report.getSummary().getMissingExternalRecords() + "\n").getBytes());
            outputStream.write(("Amount Mismatch: " + report.getSummary().getAmountMismatchRecords() + "\n").getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("Internal Transaction ID,External Transaction ID,Internal Amount,External Amount,Status,Notes\n".getBytes());
            
            for (var detail : report.getDetails()) {
                String line = String.format("%s,%s,%s,%s,%s,\"%s\"\n",
                    detail.getInternalTransactionId() != null ? detail.getInternalTransactionId() : "",
                    detail.getExternalTransactionId() != null ? detail.getExternalTransactionId() : "",
                    detail.getInternalAmount() != null ? detail.getInternalAmount() : "",
                    detail.getExternalAmount() != null ? detail.getExternalAmount() : "",
                    detail.getStatus(),
                    detail.getNotes() != null ? detail.getNotes().replace("\"", "\"\"") : ""
                );
                outputStream.write(line.getBytes());
            }
        }
        
        logger.info("Successfully exported reconciliation report to CSV for date: {}", date);
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<String> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        logger.info("Received CSV file upload: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        
        if (!file.getContentType().equals("text/csv") && 
            !file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("File must be CSV format");
        }
        
        try {
            var records = externalDataService.processCSVFile(file.getInputStream());
            
            String message = String.format("Successfully processed CSV file with %d records", records.size());
            logger.info(message);
            
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            logger.error("Failed to process CSV file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to process CSV file: " + e.getMessage());
        }
    }

    @PostMapping("/upload-json")
    public ResponseEntity<String> uploadJsonFile(@RequestParam("file") MultipartFile file) {
        logger.info("Received JSON file upload: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        
        if (!file.getContentType().equals("application/json") && 
            !file.getOriginalFilename().endsWith(".json")) {
            return ResponseEntity.badRequest().body("File must be JSON format");
        }
        
        try {
            var records = externalDataService.processJSONFile(file.getInputStream());
            
            String message = String.format("Successfully processed JSON file with %d records", records.size());
            logger.info(message);
            
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            logger.error("Failed to process JSON file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to process JSON file: " + e.getMessage());
        }
    }
}