package com.gothamdude.csv.processor.strategy;

import com.gothamdude.csv.processor.model.ProcessingResult;
import com.gothamdude.csv.processor.template.DataProcessingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Strategy for interval-based CSV processing execution
 */
@Component
@Slf4j
public class IntervalExecutionStrategy implements ExecutionStrategy {

    @Autowired
    private DataProcessingTemplate dataProcessingTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Override
    public ProcessingResult execute(String processName, String csvFilePath) {
        log.info("Starting interval CSV processing strategy for process: {}", processName);

        // Parse interval and schedule accordingly
        long intervalMinutes = parseIntervalMinutes(csvFilePath);

        log.info("Scheduling CSV processing every {} minutes for process: {}", intervalMinutes, processName);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("Running scheduled CSV processing: {}", processName);
                ProcessingResult result = dataProcessingTemplate.executeProcess(processName, csvFilePath);
                log.info("Scheduled execution completed for process: {}, success: {}",
                        processName, result.isSuccess());
            } catch (Exception e) {
                log.error("Scheduled CSV processing failed for process: {}", processName, e);
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);

        // Return immediate result for interval execution
        ProcessingResult result = new ProcessingResult(processName);
        result.markComplete(0); // Placeholder for scheduled execution

        log.info("Interval CSV processing scheduled successfully for process: {}", processName);

        return result;
    }

    private long parseIntervalMinutes(String interval) {
        log.debug("Parsing interval: {}", interval);

        // Simple interval parsing - could be enhanced
        if (interval.contains("5m")) return 5;
        if (interval.contains("10m")) return 10;
        if (interval.contains("30m")) return 30;
        if (interval.contains("1h")) return 60;
        if (interval.contains("2h")) return 120;

        long defaultInterval = 5; // Default 5 minutes
        log.debug("Using default interval: {} minutes", defaultInterval);
        return defaultInterval;
    }

    @Override
    public String getStrategyType() {
        return "interval";
    }

    @Override
    public boolean supports(String interval) {
        boolean isSupported = interval != null && (
                interval.contains("minute") ||
                        interval.contains("hour") ||
                        interval.matches("\\d+[mh]") // e.g., "5m", "1h"
        );

        log.debug("Interval strategy supports interval '{}': {}", interval, isSupported);
        return isSupported;
    }
}