package com.gothamdude.csv.processor.template;

import com.gothamdude.csv.processor.adapter.DataProcessingAdapter;
import com.gothamdude.csv.processor.model.DataProcessingConfig;
import com.gothamdude.csv.processor.model.ProcessLog;
import com.gothamdude.csv.processor.model.ProcessingResult;
import com.gothamdude.csv.processor.service.ProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Template Method pattern implementation for CSV to PostgreSQL processing workflow
 */
@Slf4j
public abstract class DataProcessingTemplate {

    @Autowired
    protected ProcessManagementService processManagementService;

    @Autowired
    protected DataProcessingAdapter dataProcessingAdapter;

    /**
     * Template method defining the fixed algorithm structure for CSV to PostgreSQL processing
     */
    public final ProcessingResult executeProcess(String processName, String csvFilePath) {
        ProcessingResult result = new ProcessingResult(processName);
        ProcessLog processLog = null;

        try {
            log.info("Starting CSV processing workflow for process: {}", processName);

            // Step 1: Initialize process (create PROCESS_LOG with 'IP' status)
            processLog = initializeProcess(processName);
            result.setRunId(processLog.getRunId());

            // Step 2: Validate inputs
            validateInputs(processName, csvFilePath);

            // Step 3: Load configuration from YAML
            DataProcessingConfig config = loadConfiguration(processName);

            // Step 4: Load CSV data using Spark SQL
            Dataset<Row> dataset = loadCsvData(csvFilePath, config);

            // Step 5: Validate loaded data
            validateLoadedData(dataset, config);

            // Step 6: Apply transformations using Spark SQL
            Dataset<Row> transformedData = transformData(dataset, config);

            // Step 7: Save to PostgreSQL table
            saveToPostgres(transformedData, config);

            // Step 8: Get final record count
            long recordCount = dataProcessingAdapter.getRecordCount(transformedData);

            // Step 9: Finalize process (update PROCESS_LOG with 'SU' status)
            finalizeProcess(processLog, "SU", null);
            result.markComplete(recordCount);

            log.info("CSV processing workflow completed successfully: {} -> PostgreSQL ({} records)",
                    csvFilePath, recordCount);

        } catch (Exception e) {
            log.error("CSV processing workflow failed for process: {}", processName, e);
            if (processLog != null) {
                finalizeProcess(processLog, "FA", e.getMessage());
            }
            result.markFailed(e.getMessage());
        }

        return result;
    }

    /**
     * Initialize process - creates process log entry with IP status (FINAL method)
     */
    protected final ProcessLog initializeProcess(String processName) {
        log.info("Initializing process: {}", processName);
        return processManagementService.initializeProcess(processName);
    }

    /**
     * Finalize process - updates process log with final status (FINAL method)
     */
    protected final void finalizeProcess(ProcessLog processLog, String status, String errorMessage) {
        log.info("Finalizing process with status: {}", status);
        processManagementService.finalizeProcess(processLog, status, errorMessage);
    }

    /**
     * Input validation (HOOK method - can be overridden)
     */
    protected void validateInputs(String processName, String csvFilePath) {
        log.info("Validating inputs: processName={}, csvFilePath={}", processName, csvFilePath);

        if (processName == null || processName.trim().isEmpty()) {
            throw new IllegalArgumentException("Process name cannot be empty");
        }
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV file path cannot be empty");
        }

        // Check if CSV file exists
        java.io.File file = new java.io.File(csvFilePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("CSV file does not exist: " + csvFilePath);
        }

        log.info("Input validation passed");
    }

    /**
     * Data validation (HOOK method - can be overridden)
     */
    protected void validateLoadedData(Dataset<Row> dataset, DataProcessingConfig config) {
        log.info("Validating loaded CSV data");

        if (!dataProcessingAdapter.validateData(dataset, config)) {
            throw new IllegalStateException("CSV data validation failed");
        }

        log.info("CSV data validation passed");
    }

    // ABSTRACT methods to be implemented by subclasses

    /**
     * Load YAML configuration for the process
     */
    protected abstract DataProcessingConfig loadConfiguration(String processName);

    /**
     * Load CSV data using Spark SQL
     */
    protected abstract Dataset<Row> loadCsvData(String csvFilePath, DataProcessingConfig config);

    /**
     * Apply transformations to the data using Spark SQL
     */
    protected abstract Dataset<Row> transformData(Dataset<Row> dataset, DataProcessingConfig config);

    /**
     * Save transformed data to PostgreSQL
     */
    protected abstract void saveToPostgres(Dataset<Row> dataset, DataProcessingConfig config);
}