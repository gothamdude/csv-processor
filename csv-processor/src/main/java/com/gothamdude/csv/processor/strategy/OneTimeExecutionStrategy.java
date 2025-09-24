package com.gothamdude.csv.processor.strategy;

import com.gothamdude.csv.processor.model.ProcessingResult;
import com.gothamdude.csv.processor.strategy.ExecutionStrategy;
import com.gothamdude.csv.processor.template.DataProcessingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Strategy for one-time CSV processing execution
 */
@Component
@Slf4j
public class OneTimeExecutionStrategy implements ExecutionStrategy {

    @Autowired
    private DataProcessingTemplate dataProcessingTemplate;

    @Override
    public ProcessingResult execute(String processName, String csvFilePath) {
        log.info("Executing one-time CSV processing strategy for process: {}", processName);
        ProcessingResult result = dataProcessingTemplate.executeProcess(processName, csvFilePath);
        log.info("One-time CSV processing completed for process: {}, success: {}",
                processName, result.isSuccess());
        return result;
    }

    @Override
    public String getStrategyType() {
        return "one-time";
    }

    @Override
    public boolean supports(String interval) {
        boolean isSupported = "once".equalsIgnoreCase(interval) ||
                "one-time".equalsIgnoreCase(interval) ||
                "single".equalsIgnoreCase(interval);
        log.debug("One-time strategy supports interval '{}': {}", interval, isSupported);
        return isSupported;
    }
}