package com.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.wallet.dto.ExternalTransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExternalDataService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalDataService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ObjectMapper objectMapper;

    public List<ExternalTransactionRecord> getExternalTransactions(LocalDate date) {
        logger.info("Fetching external transactions for date: {}", date);
        
        List<ExternalTransactionRecord> transactions = new ArrayList<>();
        
        try {
            List<ExternalTransactionRecord> csvRecords = loadFromCsv(date);
            transactions.addAll(csvRecords);
            
            List<ExternalTransactionRecord> jsonRecords = loadFromJson(date);
            transactions.addAll(jsonRecords);
            
        } catch (Exception e) {
            logger.error("Failed to load external transactions for date: {}", date, e);
        }
        
        logger.info("Loaded {} external transactions for date: {}", transactions.size(), date);
        return transactions;
    }

    public List<ExternalTransactionRecord> processCSVFile(InputStream inputStream) throws IOException, CsvException {
        logger.info("Processing CSV file for external transactions");
        
        List<ExternalTransactionRecord> records = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> rows = reader.readAll();
            
            boolean hasHeader = rows.size() > 0 && !isNumeric(rows.get(0)[2]);
            int startIndex = hasHeader ? 1 : 0;
            
            for (int i = startIndex; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 6) {
                    try {
                        ExternalTransactionRecord record = new ExternalTransactionRecord(
                            row[0].trim(), // transactionId
                            row[1].trim(), // customerId
                            new BigDecimal(row[2].trim()), // amount
                            row[3].trim(), // type
                            LocalDateTime.parse(row[4].trim(), DATE_FORMATTER), // timestamp
                            row[5].trim() // reference
                        );
                        records.add(record);
                    } catch (Exception e) {
                        logger.warn("Failed to parse CSV row {}: {}", i + 1, Arrays.toString(row), e);
                    }
                }
            }
        }
        
        logger.info("Processed {} records from CSV file", records.size());
        return records;
    }

    public List<ExternalTransactionRecord> processJSONFile(InputStream inputStream) throws IOException {
        logger.info("Processing JSON file for external transactions");
        
        ExternalTransactionRecord[] recordsArray = objectMapper.readValue(inputStream, ExternalTransactionRecord[].class);
        List<ExternalTransactionRecord> records = Arrays.asList(recordsArray);
        
        logger.info("Processed {} records from JSON file", records.size());
        return records;
    }

    public void exportReconciliationReportToCSV(List<ExternalTransactionRecord> records, OutputStream outputStream) throws IOException {
        logger.info("Exporting {} records to CSV", records.size());
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream))) {
            writer.println("Transaction ID,Customer ID,Amount,Type,Timestamp,Reference");
            
            for (ExternalTransactionRecord record : records) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                    escapeCSV(record.getTransactionId()),
                    escapeCSV(record.getCustomerId()),
                    record.getAmount(),
                    escapeCSV(record.getType()),
                    record.getTimestamp().format(DATE_FORMATTER),
                    escapeCSV(record.getReference())
                );
            }
        }
        
        logger.info("Successfully exported records to CSV");
    }

    private List<ExternalTransactionRecord> loadFromCsv(LocalDate date) {
        String filename = String.format("external_transactions_%s.csv", date);
        File file = new File("data/" + filename);
        
        if (!file.exists()) {
            logger.debug("CSV file not found for date: {}", date);
            return new ArrayList<>();
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            return processCSVFile(fis);
        } catch (Exception e) {
            logger.error("Failed to load CSV file for date: {}", date, e);
            return new ArrayList<>();
        }
    }

    private List<ExternalTransactionRecord> loadFromJson(LocalDate date) {
        String filename = String.format("external_transactions_%s.json", date);
        File file = new File("data/" + filename);
        
        if (!file.exists()) {
            logger.debug("JSON file not found for date: {}", date);
            return new ArrayList<>();
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            return processJSONFile(fis);
        } catch (Exception e) {
            logger.error("Failed to load JSON file for date: {}", date, e);
            return new ArrayList<>();
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}