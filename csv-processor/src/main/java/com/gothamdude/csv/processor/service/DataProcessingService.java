package com.gothamdude.csv.processor.service;

import com.gothamdude.csv.processor.exception.DataProcessingException;
import com.gothamdude.csv.processor.model.ProcessingResult;
import com.gothamdude.csv.processor.strategy.ExecutionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DataProcessingService {

    @Autowired
    private List<ExecutionStrategy> executionStrategies;

    @Autowired
    private ProcessManagementService processManagementService;

    /**
     * Execute CSV to PostgreSQL processing with specified strategy
     */
    public ProcessingResult executeProcessing(String processName, String csvFilePath, String interval) {
        log.info("Starting CSV to PostgreSQL processing - Process: {}, File: {}, Interval: {}",
                processName, csvFilePath, interval);

        // Check if process is already running
        if (processManagementService.isProcessRunning(processName)) {
            log.error("Process is already running: {}", processName);
            throw new DataProcessingException("Process is already running: " + processName);
        }

        // Find appropriate execution strategy
        ExecutionStrategy strategy = findStrategy(interval);

        // Execute using the selected strategy
        log.info("Executing processing using strategy: {}", strategy.getStrategyType());

        ProcessingResult result = strategy.execute(processName, csvFilePath);

        log.info("CSV processing completed - Process: {}, Success: {}, Records: {}, Duration: {}ms",
                processName, result.isSuccess(), result.getRecordsProcessed(), result.getExecutionTimeMs());

        return result;
    }

    private ExecutionStrategy findStrategy(String interval) {
        log.info("Finding execution strategy for interval: {}", interval);

        for (ExecutionStrategy strategy : executionStrategies) {
            if (strategy.supports(interval)) {
                log.info("Selected execution strategy: {} for interval: {}",
                        strategy.getStrategyType(), interval);
                return strategy;
            }
        }

        // Default to one-time execution
        log.info("No specific strategy found for interval: {}, using one-time execution", interval);

        return executionStrategies.stream()
                .filter(s -> s.supports("once"))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No execution strategy available");
                    return new DataProcessingException("No execution strategy available");
                });
    }

    public String getProcessStatus(String processName) {
        log.info("Getting process status for: {}", processName);
        return processManagementService.getLastExecutionStatus(processName);
    }
}