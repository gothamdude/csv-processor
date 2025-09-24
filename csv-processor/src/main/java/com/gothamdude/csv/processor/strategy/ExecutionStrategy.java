package com.gothamdude.csv.processor.strategy;

import com.gothamdude.csv.processor.model.ProcessingResult;

/**
 * Strategy interface for different execution modes
 */
public interface ExecutionStrategy {

    /**
     * Execute the CSV processing strategy
     */
    ProcessingResult execute(String processName, String csvFilePath);

    /**
     * Get strategy type
     */
    String getStrategyType();

    /**
     * Check if strategy supports the given interval
     */
    boolean supports(String interval);
}